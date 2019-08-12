package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.utils.EditorconfigCheckerUtils;
import com.github.imas.rdflint.utils.StringMatchUtils;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.editorconfig.checker.util.EndOfLine;
import org.editorconfig.checker.util.IndentStyle;
import org.mozilla.universalchardet.UniversalDetector;

public class FileEncodingValidator extends AbstractRdfValidator {

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
    List<Map<String, String>> params = getValidationParameterMapList();

    String filename = path.substring(parentPath.length() + 1);
    File f = new File(path);

    // check parameters
    String charset = null;
    EndOfLine eol = EndOfLine.NONE;
    boolean newLine = false;
    IndentStyle indentStyle = IndentStyle.NONE;
    int indentSize = 2;
    boolean trailingSpace = false;
    for (Map<String, String> m : params) {
      if (StringMatchUtils.matchWildcard(path, m.get("target"))) {
        // charset
        if ("utf-8".equals(m.get("charset"))) {
          charset = "UTF-8";
        } else if ("utf-16be".equals(m.get("charset"))) {
          charset = "UTF-16BE";
        } else if ("utf-16le".equals(m.get("charset"))) {
          charset = "UTF-16LE";
        }
        // end_of_line
        if ("cr".equals(m.get("end_of_line"))) {
          eol = EndOfLine.CR;
        } else if ("lf".equals(m.get("end_of_line"))) {
          eol = EndOfLine.LF;
        } else if ("crlf".equals(m.get("end_of_line"))) {
          eol = EndOfLine.CRLF;
        }
        // insert_final_newline
        if ("true".equals(m.get("insert_final_newline"))) {
          newLine = true;
        }
        // indent_style
        if ("space".equals(m.get("indent_style"))) {
          indentStyle = IndentStyle.SPACE;
        } else if ("tab".equals(m.get("indent_style"))) {
          indentStyle = IndentStyle.TAB;
        }
        // indent_size
        if (m.get("indent_size") != null) {
          try {
            indentSize = Integer.parseInt(m.get("indent_size"));
          } catch (NumberFormatException ex) {
            // ignore, and default size
          }
        }
        // trim_trailing_whitespace
        if ("true".equals(m.get("insert_final_newline"))) {
          trailingSpace = true;
        }
      }
    }

    // validation
    String encoding = null;
    try {
      encoding = UniversalDetector.detectCharset(f);
      if (encoding != null && charset != null && !charset.equals(encoding)) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            "File encoding expected " + charset + ", but " + encoding);
      }
    } catch (IOException e) {
      e.printStackTrace(); //NOPMD
    }

    if (!EditorconfigCheckerUtils.validateEol(f, eol)) {
      problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
          "End of line is not " + eol + ".");
    }

    if (newLine && (encoding == null || "UTF-8".equals(encoding))) {
      if (!EditorconfigCheckerUtils.validateFinalNewLine(f, newLine, eol)) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            "Need final new line.");
      }
    }

    if (trailingSpace) {
      if (!EditorconfigCheckerUtils.validateTrailingWhiteSpace(f)) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            "Need trailing white space.");
      }
    }

    if (!EditorconfigCheckerUtils.validateIndent(f, indentStyle, indentSize)) {
      problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
          "Need indent size is " + indentSize + " by " + indentStyle + ".");
    }

  }

}
