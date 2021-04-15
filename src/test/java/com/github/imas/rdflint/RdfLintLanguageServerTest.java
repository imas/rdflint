package com.github.imas.rdflint;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.Test;

public class RdfLintLanguageServerTest {

  @Test
  public void convertUri2FilePath() throws Exception {
    String path1 = String.join(File.separator, new String[]{"home", "user", "rdflint"});
    assertEquals(File.separator + path1,
        RdfLintLanguageServer.convertUri2FilePath("file:///home/user/rdflint"));

    String path2 = String.join(File.separator, new String[]{"C:", "rdflint"});
    assertEquals(path2,
        RdfLintLanguageServer.convertUri2FilePath("file:///C%3A/rdflint"));
  }

  @Test
  public void convertFilePath2Uri() throws Exception {
    String path1 = String.join(File.separator, new String[]{"home", "user", "rdflint"});
    assertEquals("file:///home/user/rdflint",
        RdfLintLanguageServer.convertFilePath2Uri(File.separator + path1));

    String path2 = String.join(File.separator, new String[]{"C:", "rdflint"});
    assertEquals("file:///C%3A/rdflint",
        RdfLintLanguageServer.convertFilePath2Uri(path2));
  }

  @Test
  public void diagnosticsNoTarget() throws Exception {
    RdfLintLanguageServer lsp = new RdfLintLanguageServer();
    InitializeParams initParams = new InitializeParams();
    String rootPath = this.getClass().getClassLoader().getResource("testValidatorsImpl/").getPath();
    String parentPath = rootPath + "TrimValidator/turtle_needtrim";
    initParams.setRootUri("file://" + parentPath);
    lsp.initialize(initParams);

    LanguageClient client = mock(LanguageClient.class);
    lsp.connect(client);

    lsp.refreshFileTripleSet();

    verify(client, never()).publishDiagnostics(any());
  }

  @Test
  public void diagnosticsOpen() throws Exception {
    RdfLintLanguageServer lsp = new RdfLintLanguageServer();
    InitializeParams initParams = new InitializeParams();
    String rootPath = this.getClass().getClassLoader().getResource("testValidatorsImpl/").getPath();
    String parentPath = rootPath + "TrimValidator/turtle_needtrim";
    initParams.setRootUri("file://" + parentPath);
    lsp.initialize(initParams);

    LanguageClient client = mock(LanguageClient.class);
    lsp.connect(client);

    DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams();
    openParams.setTextDocument(new TextDocumentItem());
    openParams.getTextDocument()
        .setUri(RdfLintLanguageServer.convertFilePath2Uri(parentPath + "/needtrim.rdf"));
    openParams.getTextDocument().setText("<rdf:RDF\n"
        + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "    xmlns:schema=\"http://schema.org/\"\n"
        + "    >\n"
        + "\n"
        + "  <rdf:Description rdf:about=\"something\">\n"
        + "    <schema:familyName xml:lang=\"ja\">familyName </schema:familyName>\n"
        + "  </rdf:Description>\n"
        + "\n"
        + "</rdf:RDF>");
    lsp.didOpen(openParams);

    verify(client, times(1)).publishDiagnostics(any());
  }

  @Test
  public void diagnosticsChange() throws Exception {
    RdfLintLanguageServer lsp = new RdfLintLanguageServer();
    InitializeParams initParams = new InitializeParams();
    String rootPath = this.getClass().getClassLoader().getResource("testValidatorsImpl/").getPath();
    String parentPath = rootPath + "TrimValidator/turtle_needtrim";
    System.out.println(parentPath);
    initParams.setRootUri("file://" + parentPath);
    lsp.initialize(initParams);

    LanguageClient client = mock(LanguageClient.class);
    lsp.connect(client);

    DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams();
    changeParams.setTextDocument(new VersionedTextDocumentIdentifier());
    changeParams.getTextDocument()
        .setUri(RdfLintLanguageServer.convertFilePath2Uri(parentPath + "/needtrim.rdf"));
    List<TextDocumentContentChangeEvent> changeEvents = new LinkedList<>();
    changeParams.setContentChanges(changeEvents);
    changeEvents.add(new TextDocumentContentChangeEvent());
    changeEvents.get(0).setText("<rdf:RDF\n"
        + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "    xmlns:schema=\"http://schema.org/\"\n"
        + "    >\n"
        + "\n"
        + "  <rdf:Description rdf:about=\"something\">\n"
        + "    <schema:familyName xml:lang=\"ja\">familyName </schema:familyName>\n"
        + "  </rdf:Description>\n"
        + "\n"
        + "</rdf:RDF>");
    lsp.didChange(changeParams);

    verify(client, times(1)).publishDiagnostics(any());
  }

  @Test
  public void diagnosticsChangeParseError() throws Exception {
    RdfLintLanguageServer lsp = new RdfLintLanguageServer();
    InitializeParams initParams = new InitializeParams();
    String rootPath = this.getClass().getClassLoader().getResource("testValidatorsImpl/").getPath();
    String parentPath = rootPath + "TrimValidator/turtle_needtrim";
    initParams.setRootUri("file://" + parentPath);
    lsp.initialize(initParams);

    LanguageClient client = mock(LanguageClient.class);
    lsp.connect(client);

    DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams();
    changeParams.setTextDocument(new VersionedTextDocumentIdentifier());
    changeParams.getTextDocument()
        .setUri(RdfLintLanguageServer.convertFilePath2Uri(parentPath + "/needtrim.rdf"));
    List<TextDocumentContentChangeEvent> changeEvents = new LinkedList<>();
    changeParams.setContentChanges(changeEvents);
    changeEvents.add(new TextDocumentContentChangeEvent());
    changeEvents.get(0).setText("<rdf:RDF\n"
        + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "    xmlns:schema=\"http://schema.org/\"\n"
        + "    >\n"
        + "\n"
        + "  <rdf:Description rdf:about=\"something\">\n"
        + "    <schema:familyName xml:lang=\"ja\">familyName</schema:familyN>\n"
        + "  </rdf:Description>\n"
        + "\n"
        + "</rdf:RDF>");
    lsp.didChange(changeParams);

    verify(client, times(1)).publishDiagnostics(any());
  }

  @Test
  public void diagnosticsClose() throws Exception {
    RdfLintLanguageServer lsp = new RdfLintLanguageServer();
    InitializeParams initParams = new InitializeParams();
    String rootPath = this.getClass().getClassLoader().getResource("testValidatorsImpl/").getPath();
    String parentPath = rootPath + "TrimValidator/turtle_needtrim";
    initParams.setRootUri("file://" + parentPath);
    lsp.initialize(initParams);

    LanguageClient client = mock(LanguageClient.class);
    lsp.connect(client);

    DidCloseTextDocumentParams closeParams = new DidCloseTextDocumentParams();
    closeParams.setTextDocument(new TextDocumentIdentifier());
    closeParams.getTextDocument()
        .setUri(RdfLintLanguageServer.convertFilePath2Uri(parentPath + "/needtrim.rdf"));

    lsp.didClose(closeParams);
    verify(client, times(2)).publishDiagnostics(any());
  }

}