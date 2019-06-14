package com.github.imas.rdflint;

public class LintProblem {

  public enum ErrorLevel {
    ERROR, WARN, INFO
  }

  private ErrorLevel level;
  private String message;

  LintProblem(ErrorLevel level, String message) {
    this.level = level;
    this.message = message;
  }

  public ErrorLevel getLevel() {
    return level;
  }

  public String getMessage() {
    return message;
  }

}
