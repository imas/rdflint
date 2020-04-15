package com.github.imas.rdflint;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class RdfLintTest {

  public String getParentPath(String testSet) {
    return this.getClass().getClassLoader().getResource("testRDFs/" + testSet).getPath();
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
    RdfLintParameters params = ConfigurationLoader
        .loadConfig(getParentPath("config_genok/rdflint-config.yml"));

    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("config_genok"));
    LintProblemFormatter.out(System.out, problems);

    assertEquals(1, problems.problemSize());
  }

  @Test
  public void setupParametersFromCmdOption() throws Exception {
    Map<String, String> cmdOptions = new HashMap<>();
    cmdOptions.put("outputdir", "path/outputdir");
    cmdOptions.put("baseuri", "http://example.com/base#");
    cmdOptions.put("origindir", "path/origindir");
    RdfLintParameters params = new RdfLintParameters();

    ConfigurationLoader
        .setupParameters(params, "path/targetdir", "path/parentdir", cmdOptions);

    assertEquals("getTargetDir", "path/targetdir", params.getTargetDir());
    assertEquals("getOutputDir", "path/outputdir", params.getOutputDir());
    assertEquals("getBaseUri", "http://example.com/base#", params.getBaseUri());
    assertEquals("getOriginDir", "path/origindir", params.getOriginDir());
  }

  @Test
  public void setupParametersDefault() throws Exception {
    Map<String, String> cmdOptions = new HashMap<>();

    RdfLintParameters params = new RdfLintParameters();

    ConfigurationLoader.setupParameters(params, null, "path/parentdir", cmdOptions);

    assertEquals("getTargetDir", ".", params.getTargetDir());
    assertEquals("getOutputDir", ".", params.getOutputDir());
    assertNull("getBaseUri", params.getBaseUri());
    assertNull("getOriginDir", params.getOriginDir());
  }


}
