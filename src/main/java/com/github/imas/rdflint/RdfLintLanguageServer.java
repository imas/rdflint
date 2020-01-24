package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import com.github.imas.rdflint.parser.RdflintParser;
import com.github.imas.rdflint.parser.RdflintParserRdfxml;
import com.github.imas.rdflint.parser.RdflintParserTurtle;
import com.github.imas.rdflint.validator.RdfValidator;
import com.github.imas.rdflint.validator.impl.ShaclValidator;
import com.github.imas.rdflint.validator.impl.TrimValidator;
import com.github.imas.rdflint.validator.impl.UndefinedSubjectValidator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class RdfLintLanguageServer implements LanguageServer, LanguageClientAware,
    TextDocumentService, WorkspaceService {

  /**
   * convert uri to filepath.
   */
  public static String convertUri2FilePath(String uri) {
    String fileSeparator = File.separatorChar == '\\' ? "\\\\" : File.separator;
    String fullPathEncoded = uri.substring("file://".length());
    String fullPath = Arrays.stream(fullPathEncoded.split("/")).map(raw -> {
      String decoded = raw;
      try {
        decoded = URLDecoder.decode(raw, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        // pass
      }
      return decoded;
    }).collect(Collectors.joining(fileSeparator));
    if (fullPath.split(fileSeparator)[1].endsWith(":")) {
      fullPath = fullPath.substring(1);
    }
    return fullPath;
  }

  /**
   * convert filepath to uri.
   */
  public static String convertFilePath2Uri(String filePath) {
    String fileSeparator = File.separatorChar == '\\' ? "\\\\" : File.separator;
    String prefix = filePath.charAt(0) == '/' ? "file://" : "file:///";
    return prefix + Arrays.stream(filePath.split(fileSeparator))
        .map(raw -> {
          String decoded = raw;
          try {
            decoded = URLEncoder.encode(raw, "UTF-8");
          } catch (UnsupportedEncodingException ex) {
            // pass
          }
          return decoded;
        }).collect(Collectors.joining("/"));
  }

  // show exception
  private void showException(String msg, Exception ex) {
    StringWriter w = new StringWriter();
    PrintWriter p = new PrintWriter(w);
    ex.printStackTrace(p);

    this.client.showMessage(
        new MessageParams(MessageType.Info,
            String.format("%s: %s: %s", msg, ex.getMessage(), w.toString())));
  }

  private LanguageClient client;
  private RdfLintParameters rdflintParams;
  List<RdfValidator> validators;
  Map<String, String> sourceTextMap = new ConcurrentHashMap<>();

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    // initialize rdflint
    String rootPath = convertUri2FilePath(params.getRootUri());
    String configPath = "";
    for (String fn : new String[]{
        "rdflint-config.yml",
        ".rdflint-config.yml",
        ".circleci/rdflint-config.yml"}) {
      Path path = Paths.get(rootPath + "/" + fn);
      if (Files.exists(path)) {
        configPath = path.toAbsolutePath().toString();
        break;
      }
    }
    try {
      RdfLint lint = new RdfLint();
      rdflintParams = lint.loadConfig(configPath);
      rdflintParams.setTargetDir(rootPath);
      rdflintParams.setOutputDir(rootPath);
    } catch (IOException ex) {
      showException("Error cannot initialize rdflint", ex);
    }
    validators = new LinkedList<>();
    validators.add(new TrimValidator());
    validators.add(new UndefinedSubjectValidator());
    validators.add(new ShaclValidator());
    validators.forEach(v ->
        v.setParameters(rdflintParams)
    );

    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    InitializeResult result = new InitializeResult(capabilities);
    return CompletableFuture.completedFuture(result);
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    return null;
  }

  @Override
  public void exit() {
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return this;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return this;
  }

  void diagnostics() {
    try {
      // load triple
      String parentPath = rdflintParams.getTargetDir();
      String baseUri = rdflintParams.getBaseUri();
      Map<String, List<Triple>> fileTripleSet = Files // NOPMD
          .walk(Paths.get(parentPath))
          .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
          .collect(Collectors.toConcurrentMap(
              e -> e.toString().substring(parentPath.length() + 1),
              e -> {
                Graph g = Factory.createGraphMem();
                String filename = e.toString().substring(parentPath.length() + 1);
                String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
                Lang lang = e.toString().endsWith(".ttl") ? Lang.TURTLE : Lang.RDFXML;
                String text = sourceTextMap.get(convertFilePath2Uri(e.toString()));
                if (text != null) {
                  InputStream prepareIn = new ByteArrayInputStream(
                      text.getBytes(StandardCharsets.UTF_8));
                  RDFParser.source(prepareIn).lang(lang).base(baseUri + subdir).parse(g);
                } else {
                  RDFParser.source(e.toString()).lang(lang).base(baseUri + subdir).parse(g);
                }
                List<Triple> lst = g.find().toList();
                g.close();
                return lst;
              }
          ));
      validators.forEach(v -> {
        v.prepareValidationResource(fileTripleSet);
      });

      sourceTextMap.forEach((uri, source) -> {
        // parse
        String filepath = convertUri2FilePath(uri);
        String filename = filepath.substring(parentPath.length() + 1);
        String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
        RdflintParser parser = uri.endsWith(".ttl")
            ? new RdflintParserTurtle() : new RdflintParserRdfxml(baseUri + subdir);
        validators.forEach(parser::addRdfValidator);
        List<Diagnostic> diagnosticList = parser.parse(source).stream()
            .map(p -> new Diagnostic(
                new Range(
                    new Position((int) p.getLocation().getBeginLine(),
                        (int) p.getLocation().getBeginCol()),
                    new Position((int) p.getLocation().getEndLine(),
                        (int) p.getLocation().getEndCol())),
                p.getKey())
            ).collect(Collectors.toList());

        // publish
        PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
        diagnostics.setUri(uri);
        diagnostics.setDiagnostics(diagnosticList);
        this.client.publishDiagnostics(diagnostics);
      });
    } catch (IOException ex) {
      showException("Error cannot diagnostics", ex);
    }
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    // get source
    sourceTextMap.put(params.getTextDocument().getUri(), params.getTextDocument().getText());

    // diagnostics
    diagnostics();
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    // get source
    int size = params.getContentChanges().size();
    String sourceText = params.getContentChanges().get(size - 1).getText();
    sourceTextMap.put(params.getTextDocument().getUri(), sourceText);

    // diagnostics
    diagnostics();
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    // remove source from map
    sourceTextMap.remove(params.getTextDocument().getUri());

    // clear diagnostics
    PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
    diagnostics.setUri(params.getTextDocument().getUri());
    diagnostics.setDiagnostics(new LinkedList<>());
    this.client.publishDiagnostics(diagnostics);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
  }
}
