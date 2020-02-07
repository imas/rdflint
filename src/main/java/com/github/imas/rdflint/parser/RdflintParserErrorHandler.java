package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.validator.RdfValidator;
import java.util.List;
import org.apache.jena.riot.system.ErrorHandler;

public class RdflintParserErrorHandler implements ErrorHandler {

  private List<LintProblem> parseProblemList;
  private RdfValidator validator;

  /**
   * constructor.
   */
  public RdflintParserErrorHandler(List<LintProblem> parseProblemList, RdfValidator validator) {
    this.parseProblemList = parseProblemList;
    this.validator = validator;
  }

  /**
   * constructor.
   */
  public RdflintParserErrorHandler(List<LintProblem> parseProblemList) {
    this(parseProblemList, null);
  }

  @Override
  public void warning(String message, long line, long col) {
    addDiagnostic(message, line, col, LintProblem.ErrorLevel.WARN);
  }

  @Override
  public void error(String message, long line, long col) {
    addDiagnostic(message, line, col, LintProblem.ErrorLevel.ERROR);
  }

  @Override
  public void fatal(String message, long line, long col) {
    this.error(message, line, col);
  }

  private void addDiagnostic(String message, long line, long col, LintProblem.ErrorLevel lv) {
    parseProblemList.add(new LintProblem(
        lv,
        validator,
        new LintProblemLocation(line, col),
        null, message));
  }

}
