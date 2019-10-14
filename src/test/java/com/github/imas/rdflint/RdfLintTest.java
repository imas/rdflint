package com.github.imas.rdflint;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.jena.rdf.model.Model;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class RdfLintTest {

  public String getParentPath(String testSet) {
    return this.getClass().getClassLoader().getResource("testRDFs/" + testSet).getPath();
  }

  @Test
  public void loadConfig() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(getParentPath("config_ok/rdflint-config.yml"));

    assertEquals("https://sparql.crssnky.xyz/imasrdf/", params.getBaseUri());
    assertEquals("valid.rdf", params.getRules().get(0).getTarget());
  }

  @Test
  public void loadConfigValid() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint
        .loadConfig(getParentPath("config/rdflint-config-validation.yml"));

    assertEquals("https://sparql.crssnky.xyz/imasrdf/", params.getBaseUri());
    assertEquals("value", params.getValidation().get("hoge"));
  }

  @Test
  public void degradeCheckOk() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");
    params.setOriginDir(getParentPath("validxml"));

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("originxml"));
    LintProblemFormatter.out(System.out, problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void degradeCheckNg() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");
    params.setOriginDir(getParentPath("originxml"));

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("validxml"));
    LintProblemFormatter.out(System.out, problems);

    assertEquals(1, problems.problemSize());
  }

  @Test
  public void generationRuleOk() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(getParentPath("config_genok/rdflint-config.yml"));

    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("config_genok"));
    LintProblemFormatter.out(System.out, problems);

    assertEquals(1, problems.problemSize());
  }

  @Test
  public void setupParametersFromCmdOption() throws Exception {
    CommandLine cmd = mock(CommandLine.class);
    when(cmd.getOptionValue("outputdir")).thenReturn("path/outputdir");
    when(cmd.getOptionValue("baseuri")).thenReturn("http://example.com/base#");
    when(cmd.getOptionValue("origindir")).thenReturn("path/origindir");

    RdfLintParameters params = new RdfLintParameters();

    RdfLint.setupParameters(params, cmd, "path/targetdir", "path/parentdir");

    assertEquals("getTargetDir", "path/targetdir", params.getTargetDir());
    assertEquals("getOutputDir", "path/outputdir", params.getOutputDir());
    assertEquals("getBaseUri", "http://example.com/base#", params.getBaseUri());
    assertEquals("getOriginDir", "path/origindir", params.getOriginDir());
  }

  @Test
  public void setupParametersDefault() throws Exception {
    CommandLine cmd = mock(CommandLine.class);

    RdfLintParameters params = new RdfLintParameters();

    RdfLint.setupParameters(params, cmd, null, "path/parentdir");

    assertEquals("getTargetDir", ".", params.getTargetDir());
    assertEquals("getOutputDir", ".", params.getOutputDir());
    assertNull("getBaseUri", params.getBaseUri());
    assertNull("getOriginDir", params.getOriginDir());
  }

  @Test
  public void interactiveCommand() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(getParentPath("config_ok/rdflint-config.yml"));
    params.setTargetDir(getParentPath("config_ok/"));
    String parentPath = new File(params.getTargetDir()).getCanonicalPath();
    Model m = lint.loadRdfSet(params, params.getTargetDir());

    // exit
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    assertFalse(":exit", lint.interactiveCommand(byteOut, ":exit",
        params, params.getTargetDir(), parentPath, m));
    assertEquals("exit out", 0, byteOut.toString("UTF-8").length());

    assertEquals("reload out",
        0, interactiveCommandCall(":reload", lint, params, parentPath, m).length());
    assertEquals("check out",
        0, interactiveCommandCall(":check", lint, params, parentPath, m).length());
    assertThat("help out",
        interactiveCommandCall(":help", lint, params, parentPath, m),
        CoreMatchers.containsString("help"));
    assertNotEquals("unknown out",
        0, interactiveCommandCall(":unknown", lint, params, parentPath, m).length());

    assertThat("select out",
        interactiveCommandCall("select ?s ?p ?o where {?s ?p ?o}", lint, params, parentPath, m),
        CoreMatchers.containsString("familyName"));
    assertThat("describe out",
        interactiveCommandCall("describe <https://sparql.crssnky.xyz/imasrdf/something>",
            lint, params, parentPath, m),
        CoreMatchers.containsString("\"familyName\"@ja"));
    assertThat("ask out",
        interactiveCommandCall("ask {<https://sparql.crssnky.xyz/imasrdf/something> ?p ?o}", lint,
            params, parentPath, m),
        CoreMatchers.containsString("true"));
  }

  private String interactiveCommandCall(
      String line, RdfLint lint, RdfLintParameters params, String parentPath, Model m)
      throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    boolean rtn = lint
        .interactiveCommand(byteOut, line, params, params.getTargetDir(), parentPath, m);
    assertTrue(line, rtn);
    return byteOut.toString("UTF-8");
  }

}
