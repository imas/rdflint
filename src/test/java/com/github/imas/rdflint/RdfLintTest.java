package com.github.imas.rdflint;

import static junit.framework.TestCase.assertEquals;

import com.github.imas.rdflint.config.RdfLintParameters;
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

}
