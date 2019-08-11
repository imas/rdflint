package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.utils.EditorconfigCheckerUtils;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.io.File;
import org.editorconfig.checker.util.EndOfLine;

public class FileEncodingValidator extends AbstractRdfValidator {

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
    String filename = path.substring(parentPath.length() + 1);
    File f = new File(path);
    boolean rtnEol = EditorconfigCheckerUtils.validateEol(f, EndOfLine.LF);
    if (!rtnEol) {
      problems.addProblem(filename, LintProblem.ErrorLevel.INFO,
          "End of line is not LF.");
    }

    boolean rtnNL = EditorconfigCheckerUtils.validateFinalNewLine(f, true, EndOfLine.LF);
    if (!rtnNL) {
      problems.addProblem(filename, LintProblem.ErrorLevel.INFO,
          "Need final new line.");
    }

    boolean rtnTW = EditorconfigCheckerUtils.validateTrailingWhiteSpace(f);
    if (!rtnTW) {
      problems.addProblem(filename, LintProblem.ErrorLevel.INFO,
          "Need trailing white space.");
    }
  }

}
