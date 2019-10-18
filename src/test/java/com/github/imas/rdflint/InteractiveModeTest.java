package com.github.imas.rdflint;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.hamcrest.CoreMatchers;
import org.jline.reader.Candidate;
import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser.ParseContext;
import org.junit.Test;

public class InteractiveModeTest {

  public String getParentPath(String testSet) {
    return this.getClass().getClassLoader().getResource("testRDFs/" + testSet).getPath();
  }

  @Test
  public void interactiveCommand() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(getParentPath("config_ok/rdflint-config.yml"));
    params.setTargetDir(getParentPath("config_ok/"));
    Model m = DatasetLoader.loadRdfSet(params, params.getTargetDir());

    // command
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    assertFalse(":exit", InteractiveMode.interactiveCommand(byteOut, ":exit",
        params, params.getTargetDir(), m));
    assertEquals("exit out", 0, byteOut.toString("UTF-8").length());

    assertEquals("reload out",
        0, interactiveCommandCall(":reload", lint, params, m).length());
    assertEquals("check out",
        0, interactiveCommandCall(":check", lint, params, m).length());
    assertThat("help out",
        interactiveCommandCall(":help", lint, params, m),
        CoreMatchers.containsString("help"));
    assertNotEquals("unknown out",
        0, interactiveCommandCall(":unknown", lint, params, m).length());

    // query
    assertThat("select out",
        interactiveCommandCall("select ?s ?p ?o where {?s ?p ?o}", lint, params, m),
        CoreMatchers.containsString("familyName"));
    assertThat("describe out",
        interactiveCommandCall("describe <https://sparql.crssnky.xyz/imasrdf/something>",
            lint, params, m),
        CoreMatchers.containsString("\"familyName\"@ja"));
    assertThat("ask out",
        interactiveCommandCall("ask {<https://sparql.crssnky.xyz/imasrdf/something> ?p ?o}", lint,
            params, m),
        CoreMatchers.containsString("true"));
  }

  private String interactiveCommandCall(
      String line, RdfLint lint, RdfLintParameters params, Model m)
      throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    boolean rtn = InteractiveMode
        .interactiveCommand(byteOut, line, params, params.getTargetDir(), m);
    assertTrue(line, rtn);
    return byteOut.toString("UTF-8");
  }

  // Interactive Parser
  @Test
  public void interactiveParserCommand() throws Exception {
    InteractiveMode.InteractiveParser parser = new InteractiveMode.InteractiveParser();
    ParsedLine pl = parser.parse(":exit", 0, ParseContext.ACCEPT_LINE);
    assertEquals(":exit", pl.line());
  }

  @Test(expected = EOFError.class)
  public void interactiveParserNoCommand() throws Exception {
    InteractiveMode.InteractiveParser parser = new InteractiveMode.InteractiveParser();
    parser.parse("", 0, ParseContext.ACCEPT_LINE);
  }

  @Test
  public void interactiveParserDoubleReturn() throws Exception {
    InteractiveMode.InteractiveParser parser = new InteractiveMode.InteractiveParser();
    ParsedLine pl = parser.parse("select\n", 0, ParseContext.ACCEPT_LINE);
    assertEquals("select\n", pl.line());
  }

  @Test(expected = EOFError.class)
  public void interactiveParserNoDoubleReturn() throws Exception {
    InteractiveMode.InteractiveParser parser = new InteractiveMode.InteractiveParser();
    parser.parse("select", 0, ParseContext.ACCEPT_LINE);
  }

  // Interactive Completer
  @Test
  public void interactiveCompleterCommand() throws Exception {
    ParsedLine pl = mock(ParsedLine.class);
    when(pl.line()).thenReturn(":ex");
    when(pl.word()).thenReturn(":ex");
    InteractiveMode.InteractiveCompleter completer = new InteractiveMode.InteractiveCompleter();
    List<Candidate> candidates = new LinkedList<>();
    completer.complete(null, pl, candidates);
    assertEquals(":exit", candidates.get(0).value());
  }

  @Test
  public void interactiveCompleterQuery() throws Exception {
    ParsedLine pl = mock(ParsedLine.class);
    when(pl.line()).thenReturn("se");
    when(pl.word()).thenReturn("se");
    InteractiveMode.InteractiveCompleter completer = new InteractiveMode.InteractiveCompleter();
    List<Candidate> candidates = new LinkedList<>();
    completer.complete(null, pl, candidates);
    assertEquals("SELECT", candidates.get(0).value());
  }

  @Test
  public void interactiveCompleterPrefixAlias() throws Exception {
    ParsedLine pl = mock(ParsedLine.class);
    when(pl.line()).thenReturn("PREFIX foa");
    when(pl.words()).thenReturn(Arrays.asList("PREFIX", "foa"));
    when(pl.word()).thenReturn("foa");
    InteractiveMode.InteractiveCompleter completer = new InteractiveMode.InteractiveCompleter();
    List<Candidate> candidates = new LinkedList<>();
    completer.complete(null, pl, candidates);
    assertEquals("foaf:", candidates.get(0).value());
  }

  @Test
  public void interactiveCompleterPrefixUri() throws Exception {
    ParsedLine pl = mock(ParsedLine.class);
    when(pl.line()).thenReturn("PREFIX rdfs: ");
    when(pl.words()).thenReturn(Arrays.asList("PREFIX", "rdfs:", ""));
    when(pl.word()).thenReturn("");
    InteractiveMode.InteractiveCompleter completer = new InteractiveMode.InteractiveCompleter();
    List<Candidate> candidates = new LinkedList<>();
    completer.complete(null, pl, candidates);
    assertEquals("<http://www.w3.org/2000/01/rdf-schema#>", candidates.get(0).value());
  }

}
