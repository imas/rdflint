package com.github.takemikami.rdflint;

import static junit.framework.TestCase.assertEquals;

import org.junit.Assert;
import org.junit.Test;

public class RDFLintTest {

  public String getParentPath(String testSet) {
    return this.getClass().getClassLoader().getResource("testRDFs/" + testSet).getPath();
  }

  @Test
  public void loadConfig() throws Exception {
    RDFLint lint = new RDFLint();
    RDFLintParameters params = lint.loadConfig(getParentPath("config_ok/rdflint-config.yml"));

    assertEquals("https://sparql.crssnky.xyz/imasrdf/", params.getBaseUri());
    assertEquals("valid.rdf", params.getRules().get(0).getTarget());
  }

  @Test
  public void validxml() throws Exception {
    RDFLintParameters params = new RDFLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RDFLint lint = new RDFLint();
    LintProblemSet problems = lint.lintRDFDataSet(params, getParentPath("validxml"));
    lint.printLintProblem(problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void invalidxml() throws Exception {
    RDFLintParameters params = new RDFLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RDFLint lint = new RDFLint();
    LintProblemSet problems = lint.lintRDFDataSet(params, getParentPath("invalidxml"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"invalidxml.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void undefinedsubject() throws Exception {
    RDFLintParameters params = new RDFLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RDFLint lint = new RDFLint();
    LintProblemSet problems = lint.lintRDFDataSet(params, getParentPath("undefinedsubject"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"undefinedsubject.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void customRuleOk() throws Exception {
    RDFLint lint = new RDFLint();
    RDFLintParameters params = lint.loadConfig(getParentPath("config_ok/rdflint-config.yml"));
    LintProblemSet problems = lint.lintRDFDataSet(params, getParentPath("config_ok"));
    lint.printLintProblem(problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void customRuleNg() throws Exception {
    RDFLint lint = new RDFLint();
    RDFLintParameters params = lint.loadConfig(getParentPath("config_ng/rdflint-config.yml"));
    LintProblemSet problems = lint.lintRDFDataSet(params, getParentPath("config_ng"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
  }

}
