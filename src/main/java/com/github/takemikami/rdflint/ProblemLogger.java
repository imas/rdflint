package com.github.takemikami.rdflint;

// Problem Logger for groovy
public class ProblemLogger {

  LintProblemSet set;
  String file;
  String name;

  /**
   * problem logger constructor.
   */
  public ProblemLogger(LintProblemSet set, String file, String name) {
    this.set = set;
    this.file = file;
    this.name = name;
  }

  public void error(String msg) {
    set.addProblem(this.file, LintProblemSet.ERROR, name + ": " + msg);
  }

  public void warn(String msg) {
    set.addProblem(this.file, LintProblemSet.WARNING, name + ": " + msg);
  }

}
