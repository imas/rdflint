package com.github.imas.rdflint;

import static junit.framework.TestCase.assertEquals;

import com.github.imas.rdflint.config.RdfLintParameters;
import org.junit.Assert;
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
  public void validxml() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("validxml"));
    lint.printLintProblem(problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void invalidxml() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("invalidxml"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"invalidxml.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void undefinedsubject() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("undefinedsubject"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"undefinedsubject.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void undefinedsubjectSchemaOrg() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint
        .lintRdfDataSet(params, getParentPath("undefinedsubject_resource"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"undefinedsubject_resource.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void datatypeOk() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("datatype_ok"));
    lint.printLintProblem(problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void datatypeNg() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("datatype_ng"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"datatype_ng.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void needtrim() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("needtrim"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"needtrim.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void customRuleOk() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(getParentPath("config_ok/rdflint-config.yml"));
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("config_ok"));
    lint.printLintProblem(problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void customRuleNg() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(getParentPath("config_ng/rdflint-config.yml"));
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("config_ng"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
  }

  @Test
  public void degradeCheckOk() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");
    params.setOriginDir(getParentPath("validxml"));

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("originxml"));
    lint.printLintProblem(problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void degradeCheckNg() throws Exception {
    RdfLintParameters params = new RdfLintParameters();
    params.setBaseUri("https://sparql.crssnky.xyz/imasrdf/");
    params.setOriginDir(getParentPath("originxml"));

    RdfLint lint = new RdfLint();
    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("validxml"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
  }

  @Test
  public void generationRuleOk() throws Exception {
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(getParentPath("config_genok/rdflint-config.yml"));

    LintProblemSet problems = lint.lintRdfDataSet(params, getParentPath("config_genok"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
  }


}
