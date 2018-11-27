package com.github.takemikami.rdflint;

import static junit.framework.TestCase.assertEquals;

import org.junit.Assert;
import org.junit.Test;

public class RDFLintTest {

  public String getParentPath(String testSet) {
    return this.getClass().getClassLoader().getResource("testRDFs/" + testSet).getPath();
  }

  @Test
  public void valid() throws Exception {
    String baseUri = "https://sparql.crssnky.xyz/imasrdf/";
    RDFLint lint = new RDFLint();
    LintProblemSet problems = lint.lintRDFDataSet(baseUri, getParentPath("validxml"));
    lint.printLintProblem(problems);

    assertEquals(0, problems.problemSize());
  }

  @Test
  public void invalidxml() throws Exception {
    String baseUri = "https://sparql.crssnky.xyz/imasrdf/";

    RDFLint lint = new RDFLint();
    LintProblemSet problems = lint.lintRDFDataSet(baseUri, getParentPath("invalidxml"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"invalidxml.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }

  @Test
  public void undefinedsubject() throws Exception {
    String baseUri = "https://sparql.crssnky.xyz/imasrdf/";
    RDFLint lint = new RDFLint();
    LintProblemSet problems = lint.lintRDFDataSet(baseUri, getParentPath("undefinedsubject"));
    lint.printLintProblem(problems);

    assertEquals(1, problems.problemSize());
    Assert.assertArrayEquals(
        new String[]{"undefinedsubject.rdf"},
        problems.getProblemSet().keySet().toArray(new String[]{}));
  }


}
