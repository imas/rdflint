package com.github.imas.rdflint;

import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.config.RdfLintParameters;
import com.github.imas.rdflint.parser.RdflintParser;
import com.github.imas.rdflint.validator.RdfValidator;
import com.github.imas.rdflint.validator.impl.ShaclValidator;
import com.github.imas.rdflint.validator.impl.TrimValidator;
import com.github.imas.rdflint.validator.impl.UndefinedSubjectValidator;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
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
    String prefix = File.separatorChar == '\\' ? "file:///" : "file://";
    String fullPathEncoded = uri.substring(prefix.length());
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
    for (String fn : ConfigurationLoader.CONFIG_SEARCH_PATH) {
      Path path = Paths.get(rootPath + "/" + fn);
      if (Files.exists(path)) {
        configPath = path.toAbsolutePath().toString();
        break;
      }
    }
    try {
      rdflintParams = ConfigurationLoader.loadConfig(configPath);
      rdflintParams.setTargetDir(rootPath);
      rdflintParams.setOutputDir(rootPath);
      if (rdflintParams.getSuppressPath() == null) {
        for (String fn : ConfigurationLoader.SUPPRESS_SEARCH_PATH) {
          Path path = Paths.get(rootPath + "/" + fn);
          if (Files.exists(path)) {
            rdflintParams.setSuppressPath(path.toAbsolutePath().toString());
            break;
          }
        }
      }
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
    refreshFileTripleSet();

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

  private DiagnosticSeverity convertLintProblemLevel2DiagnosticSeverity(ErrorLevel lv) {
    DiagnosticSeverity severity;
    switch (lv) {
      case ERROR:
        severity = DiagnosticSeverity.Error;
        break;
      case WARN:
        severity = DiagnosticSeverity.Warning;
        break;
      default:
        severity = DiagnosticSeverity.Information;
    }
    return severity;
  }

  private List<Diagnostic> convertLintProblem2DiagnosticList(List<LintProblem> problems) {
    return problems.stream()
        .map(p -> new Diagnostic(
            new Range(
                new Position((int) p.getLocation().getBeginLine() - 1,
                    (int) p.getLocation().getBeginCol() - 1),
                new Position((int) p.getLocation().getEndLine() - 1,
                    (int) p.getLocation().getEndCol() - 1)),
            LintProblemFormatter
                .dumpMessage(p.getKey(), null, LintProblemFormatter.buildArguments(p)),
            convertLintProblemLevel2DiagnosticSeverity(p.getLevel()),
            "rdflint")
        ).collect(Collectors.toList());
  }

  Map<String, List<Triple>> fileTripleSet;

  void refreshFileTripleSet() {
    try {
      // load triple
      String parentPath = rdflintParams.getTargetDir();
      String baseUri = rdflintParams.getBaseUri();
      fileTripleSet = Files // NOPMD
          .walk(Paths.get(parentPath))
          .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
          .collect(Collectors.toConcurrentMap(
              e -> e.toString().substring(parentPath.length() + 1),
              e -> {
                Graph g = Factory.createGraphMem();
                String filename = e.toString().substring(parentPath.length() + 1);
                String subdir = filename.substring(0, filename.lastIndexOf(File.separator) + 1);
                if (File.separatorChar == '\\') {
                  subdir = filename.replaceAll("\\\\", "/");
                }
                Lang lang = e.toString().endsWith(".ttl") ? Lang.TURTLE : Lang.RDFXML;
                String text = sourceTextMap.get(convertFilePath2Uri(e.toString()));
                List<Triple> lst;
                List<LintProblem> problems = new LinkedList<>();
                try {
                  (text != null ? RdflintParser.fromString(text) : RdflintParser.source(e))
                      .lang(lang)
                      .base(baseUri + subdir)
                      .parse(g, problems);
                  lst = g.find().toList();
                } catch (IOException ex) {
                  lst = new LinkedList<>();
                }
                return lst;
              }
          ));
    } catch (IOException ex) {
      showException("Error cannot diagnostics", ex);
    }
  }

  void diagnostics(String changedUri) {
    // load triple
    String changedFilePath = convertUri2FilePath(changedUri);
    String parentPath = rdflintParams.getTargetDir();
    String baseUri = rdflintParams.getBaseUri();
    {
      Graph g = Factory.createGraphMem();
      String filename = changedFilePath.substring(parentPath.length() + 1);
      String subdir = filename.substring(0, filename.lastIndexOf(File.separator) + 1);
      if (File.separatorChar == '\\') {
        subdir = filename.replaceAll("\\\\", "/");
      }
      Lang lang = changedFilePath.endsWith(".ttl") ? Lang.TURTLE : Lang.RDFXML;
      String text = sourceTextMap.get(convertFilePath2Uri(changedFilePath));
      List<LintProblem> problems = new LinkedList<>();
      try {
        (text != null ? RdflintParser.fromString(text)
            : RdflintParser.source(Paths.get(changedFilePath)))
            .lang(lang)
            .base(baseUri + subdir)
            .parse(g, problems);
        List<Triple> tripleSet = g.find().toList();
        String key = changedFilePath.substring(parentPath.length() + 1);
        fileTripleSet.put(key, tripleSet);
      } catch (Exception ex) {
        if (problems.isEmpty()) {
          problems.add(new LintProblem(
              ErrorLevel.WARN,
              null,
              new LintProblemLocation(1, 1, 1, 1),
              null,
              ex.getMessage()));
        }
      } finally {
        g.close();
      }
      if (!problems.isEmpty()) {
        List<Diagnostic> diagnosticList = convertLintProblem2DiagnosticList(problems);
        PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
        diagnostics.setUri(changedUri);
        diagnostics.setDiagnostics(diagnosticList);
        this.client.publishDiagnostics(diagnostics);
        return;
      }
    }
    // diagnostics
    validators.forEach(v -> {
      v.prepareValidationResource(fileTripleSet);
    });
    sourceTextMap.forEach((uri, source) -> {
      // parse
      String filepath = convertUri2FilePath(uri);
      String filename = filepath.substring(parentPath.length() + 1);
      String subdir = filename.substring(0, filename.lastIndexOf(File.separator) + 1);
      if (File.separatorChar == '\\') {
        subdir = filename.replaceAll("\\\\", "/");
      }

      Graph g = Factory.createGraphMem();
      List<LintProblem> problems = new LinkedList<>();
      Lang lang = uri.endsWith(".ttl") ? Lang.TURTLE : Lang.RDFXML;
      RdflintParser.fromString(source)
          .lang(lang)
          .base(baseUri + subdir)
          .validators(validators)
          .parse(g, problems);
      LintProblemSet problemSet = new LintProblemSet();
      problems.forEach(p -> {
        problemSet.addProblem(filename, p);
      });

      // suppress problems
      try {
        LintProblemSet filtered = ValidationRunner
            .suppressProblems(problemSet, rdflintParams.getSuppressPath());
        problems = filtered.getProblemSet().get(filename);
        if (problems == null) {
          problems = new LinkedList<>();
        }
      } catch (IOException ex) {
        // pass
      }
      List<Diagnostic> diagnosticList = convertLintProblem2DiagnosticList(problems);

      // publish
      PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
      diagnostics.setUri(uri);
      diagnostics.setDiagnostics(diagnosticList);
      this.client.publishDiagnostics(diagnostics);
    });
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    // get source
    sourceTextMap.put(params.getTextDocument().getUri(), params.getTextDocument().getText());

    // diagnostics
    diagnostics(params.getTextDocument().getUri());
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    // get source
    int size = params.getContentChanges().size();
    String sourceText = params.getContentChanges().get(size - 1).getText();
    sourceTextMap.put(params.getTextDocument().getUri(), sourceText);

    // diagnostics
    diagnostics(params.getTextDocument().getUri());
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

    // diagnostics
    diagnostics(params.getTextDocument().getUri());
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    // refresh all tripleset
    refreshFileTripleSet();
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
  }
}
