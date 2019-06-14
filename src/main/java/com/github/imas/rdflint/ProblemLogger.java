package com.github.imas.rdflint;

// Problem Logger for groovy
public class ProblemLogger {

  private LintProblemSet set;
  private String file;
  private String name;

  /**
   * problem logger constructor.
   */
  public ProblemLogger(LintProblemSet set, String file, String name) {
    this.set = set;
    this.file = file;
    this.name = name;
  }

  public void error(String msg) {
    set.addProblem(this.file, LintProblem.ErrorLevel.ERROR, name + ": " + msg);
  }

  public void warn(String msg) {
    set.addProblem(this.file, LintProblem.ErrorLevel.WARN, name + ": " + msg);
  }

  public void info(String msg) {
    set.addProblem(this.file, LintProblem.ErrorLevel.INFO, name + ": " + msg);
  }

}
