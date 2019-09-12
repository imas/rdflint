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
import java.util.Locale;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileEncodingValidatorTest {

  private String getParentPath() {
    return this.getClass().getClassLoader().getResource("testRDFs/fileencoding").getPath();
  }

  private LintProblemSet callFileEncodingValidate(String rdfname, RdfLintParameters param) {
    LintProblemSet problems = new LintProblemSet();
    FileEncodingValidator validator = new FileEncodingValidator();
    validator.setParameters(param);
    validator.validateFile(problems, getParentPath() + "/" + rdfname, getParentPath());
    return problems;
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

  @BeforeClass
  public static void beforeClass() {
    Locale.setDefault(Locale.ENGLISH);
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
    LintProblemSet problems = callFileEncodingValidate(rdfname, param);

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
    LintProblemSet problems = callFileEncodingValidate(rdfname, param);

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
    LintProblemSet problems = callFileEncodingValidate(rdfname, param);

    // validate
    assertFalse(problems.hasError());
  }

  @Test
  public void eol() throws Exception {
    // prepare
    List<Map<String, String>> validationParams = new LinkedList<>();
    validationParams.add(new HashMap<>());
    validationParams.get(0).put("target", "*");
    validationParams.get(0).put("end_of_line", "lf");
    RdfLintParameters param = createRdfLintParameters(validationParams);

    // call & validate 1
    String rdfname1 = "lf.txt";
    LintProblemSet problems1 = callFileEncodingValidate(rdfname1, param);
    assertFalse(problems1.hasError());

    // call & validate 2
    String rdfname2 = "crlf.txt";
    LintProblemSet problems2 = callFileEncodingValidate(rdfname2, param);
    assertTrue(problems2.hasError());
    assertEquals(1, problems2.getProblemSet().get(rdfname2).size());
    assertTrue(problems2.getProblemSet().get(rdfname2).get(0).getMessage().indexOf("LF") > 0);
  }


  @Test
  public void finalNewLine() throws Exception {
    // prepare
    List<Map<String, String>> validationParams = new LinkedList<>();
    validationParams.add(new HashMap<>());
    validationParams.get(0).put("target", "*");
    validationParams.get(0).put("insert_final_newline", "true");
    RdfLintParameters param = createRdfLintParameters(validationParams);

    // call & validate 1
    String rdfname1 = "finalnewline_exists.txt";
    LintProblemSet problems1 = callFileEncodingValidate(rdfname1, param);
    assertFalse(problems1.hasError());

    // call & validate 2
    String rdfname2 = "finalnewline_noexists.txt";
    LintProblemSet problems2 = callFileEncodingValidate(rdfname2, param);
    dumpLintProblem(problems2.getProblemSet().get(rdfname2));
    assertTrue(problems2.hasError());
    assertEquals(1, problems2.getProblemSet().get(rdfname2).size());
    assertTrue(
        problems2.getProblemSet().get(rdfname2).get(0).getMessage().indexOf("final new line") > 0);
  }


  @Test
  public void trailingWhiteSpace() throws Exception {
    // prepare
    List<Map<String, String>> validationParams = new LinkedList<>();
    validationParams.add(new HashMap<>());
    validationParams.get(0).put("target", "*");
    validationParams.get(0).put("trim_trailing_whitespace", "true");
    RdfLintParameters param = createRdfLintParameters(validationParams);

    // call & validate 1
    String rdfname1 = "trailingwhitespace_noexists.txt";
    LintProblemSet problems1 = callFileEncodingValidate(rdfname1, param);
    assertFalse(problems1.hasError());

    // call & validate 2
    String rdfname2 = "trailingwhitespace_exists.txt";
    LintProblemSet problems2 = callFileEncodingValidate(rdfname2, param);
    assertTrue(problems2.hasError());
    dumpLintProblem(problems2.getProblemSet().get(rdfname2));
    assertEquals(1, problems2.getProblemSet().get(rdfname2).size());
    assertTrue(
        problems2.getProblemSet().get(rdfname2).get(0).getMessage().indexOf("white space") > 0);
  }

  @Test
  public void indent() throws Exception {
    // prepare
    List<Map<String, String>> validationParams = new LinkedList<>();
    validationParams.add(new HashMap<>());
    validationParams.get(0).put("target", "*");
    validationParams.get(0).put("indent_style", "space");
    validationParams.get(0).put("indent_size", "2");
    RdfLintParameters param = createRdfLintParameters(validationParams);

    // call & validate 1
    String rdfname1 = "indent_space2.txt";
    LintProblemSet problems1 = callFileEncodingValidate(rdfname1, param);
    assertFalse(problems1.hasError());

    // call & validate 2
    String rdfname2 = "indent_tab.txt";
    LintProblemSet problems2 = callFileEncodingValidate(rdfname2, param);
    dumpLintProblem(problems2.getProblemSet().get(rdfname2));
    assertTrue(problems2.hasError());
    assertEquals(1, problems2.getProblemSet().get(rdfname2).size());
    assertTrue(problems2.getProblemSet().get(rdfname2).get(0).getMessage().indexOf("SPACE") > 0);
  }

}
