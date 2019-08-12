package com.github.imas.rdflint.utils;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.File;
import org.editorconfig.checker.util.EndOfLine;
import org.editorconfig.checker.util.IndentStyle;
import org.junit.Test;

public class EditorconfigCheckerUtilsTest {

  private File getTestFile(String testFile) {
    return new File(
        this.getClass().getClassLoader()
            .getResource("testUtils/editorconfig/" + testFile).getPath());
  }

  @Test
  public void validateEolTest() throws Exception {
    // lf.txt
    assertTrue("lf.txt is lf",
        EditorconfigCheckerUtils.validateEol(getTestFile("lf.txt"), EndOfLine.LF));
    assertFalse("lf.txt is not crlf",
        EditorconfigCheckerUtils.validateEol(getTestFile("lf.txt"), EndOfLine.CRLF));

    // crlf.txt
    assertTrue("crlf.txt is crlf",
        EditorconfigCheckerUtils.validateEol(getTestFile("crlf.txt"), EndOfLine.CRLF));
    assertFalse("crlf.txt is not crlf",
        EditorconfigCheckerUtils.validateEol(getTestFile("crlf.txt"), EndOfLine.LF));
  }

  @Test
  public void validateFinalNewLineTest() throws Exception {
    // finalnewline_exists.txt
    assertTrue("finalnewline_exists.txt is true",
        EditorconfigCheckerUtils
            .validateFinalNewLine(getTestFile("finalnewline_exists.txt"), true, EndOfLine.LF));

    // finalnewline_noexists.txt
    assertFalse("finalnewline_noexists.txt is false",
        EditorconfigCheckerUtils
            .validateFinalNewLine(getTestFile("finalnewline_noexists.txt"), true, EndOfLine.LF));
  }

  @Test
  public void validateTrailingWhiteSpaceTest() throws Exception {
    // trailingwhitespace_exist.txt
    assertFalse("trailingwhitespace_exist.txt is false",
        EditorconfigCheckerUtils
            .validateTrailingWhiteSpace(getTestFile("trailingwhitespace_exists.txt")));

    // trailingwhitespace_noexist.txt
    assertTrue("trailingwhitespace_exist.txt is true",
        EditorconfigCheckerUtils
            .validateTrailingWhiteSpace(getTestFile("trailingwhitespace_noexists.txt")));
  }

  @Test
  public void validateIndentTest() throws Exception {
    // indent_space2.txt
    assertTrue("indent_space2.txt is 2 space indent",
        EditorconfigCheckerUtils
            .validateIndent(getTestFile("indent_space2.txt"), IndentStyle.SPACE, 2));
    assertFalse("indent_space2.txt is not 4 space indent",
        EditorconfigCheckerUtils
            .validateIndent(getTestFile("indent_space2.txt"), IndentStyle.SPACE, 4));
    assertFalse("indent_space2.txt is not tab indent",
        EditorconfigCheckerUtils
            .validateIndent(getTestFile("indent_space2.txt"), IndentStyle.TAB, 1));

    // indent_tab.txt
    assertTrue("indent_tab.txt is tab indent",
        EditorconfigCheckerUtils
            .validateIndent(getTestFile("indent_tab.txt"), IndentStyle.TAB, 1));
    assertFalse("indent_tab.txt is not space indent",
        EditorconfigCheckerUtils
            .validateIndent(getTestFile("indent_tab.txt"), IndentStyle.SPACE, 2));
  }

}
