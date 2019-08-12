package com.github.imas.rdflint.validator.impl;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.config.RdfLintParameters;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class FileEncodingValidatorTest {

  private String getParentPath() {
    return this.getClass().getClassLoader().getResource("testRDFs/fileencoding").getPath();
  }

  private static RdfLintParameters createRdfLintParameters(List<Map<String, String>> maps) {
    RdfLintParameters param = new RdfLintParameters();
    if (maps == null) {
      return param;
    }
    param.setValidation(new HashMap<>());
    List<Map<String, String>> validationParams = new LinkedList<>(maps);
    param.getValidation().put("fileEncoding", validationParams);
    return param;
  }

  private void dumpLintProblem(List<LintProblem> problems) {
    for (LintProblem problem : problems) {
      System.out.println(problem.getLevel() + "\t" + problem.getMessage());
    }
  }

  @Test
  public void encodingUtf8_Ok() throws Exception {
    // prepare
    List<Map<String, String>> validationParams = new LinkedList<>();
    validationParams.add(new HashMap<>());
    validationParams.get(0).put("target", "*");
    validationParams.get(0).put("charset", "utf-8");
    RdfLintParameters param = createRdfLintParameters(validationParams);

    // call
    String rdfname = "utf8.rdf";
    LintProblemSet problems = new LintProblemSet();
    FileEncodingValidator validator = new FileEncodingValidator();
    validator.setParameters(param);
    validator.validateFile(problems, getParentPath() + "/" + rdfname, getParentPath());

    // validate
    assertFalse(problems.hasError());
  }

  @Test
  public void encodingUtf8_Ng() throws Exception {
    // prepare
    List<Map<String, String>> validationParams = new LinkedList<>();
    validationParams.add(new HashMap<>());
    validationParams.get(0).put("target", "*");
    validationParams.get(0).put("charset", "utf-16le");
    RdfLintParameters param = createRdfLintParameters(validationParams);

    // call
    String rdfname = "utf8.rdf";
    LintProblemSet problems = new LintProblemSet();
    FileEncodingValidator validator = new FileEncodingValidator();
    validator.setParameters(param);
    validator.validateFile(problems, getParentPath() + "/" + rdfname, getParentPath());

    // validate
    assertTrue(problems.hasError());
    assertEquals(1, problems.getProblemSet().get(rdfname).size());
    assertTrue(problems.getProblemSet().get(rdfname).get(0).getMessage().indexOf("UTF-16LE") > 0);
  }

  @Test
  public void encodingUtf8_Ok_skip() throws Exception {
    // prepare
    List<Map<String, String>> validationParams = new LinkedList<>();
    validationParams.add(new HashMap<>());
    validationParams.get(0).put("target", "*.ttl");
    validationParams.get(0).put("charset", "utf-16le");
    RdfLintParameters param = createRdfLintParameters(validationParams);

    // call
    String rdfname = "utf8.rdf";
    LintProblemSet problems = new LintProblemSet();
    FileEncodingValidator validator = new FileEncodingValidator();
    validator.setParameters(param);
    validator.validateFile(problems, getParentPath() + "/" + rdfname, getParentPath());

    // validate
    assertFalse(problems.hasError());
  }

}
