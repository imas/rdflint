package com.github.imas.rdflint;

import com.github.imas.rdflint.validator.RdfValidator;
import java.util.LinkedList;
import java.util.List;

public class LintProblem {

  public enum ErrorLevel {
    ERROR, WARN, INFO
  }

  private ErrorLevel level;
  private String key;
  private LintProblemLocation location;
  private Object[] arguments;

  /**
   * constructor.
   */
  public LintProblem(ErrorLevel level, RdfValidator validator, LintProblemLocation location,
      String key, Object... arguments) {
    if (validator == null || key == null) {
      this.key = "com.github.imas.rdflint.validator.impl.parseWarning";
    } else {
      String pkgName = validator.getClass().getPackage().getName();
      this.key = pkgName + "." + key;
    }
    this.location = location;
    this.level = level;
    this.arguments = arguments.clone();
  }

  // location
  public LintProblemLocation getLocation() {
    return location;
  }

  public void setLocation(LintProblemLocation location) {
    this.location = location;
  }

  // level
  public ErrorLevel getLevel() {
    return level;
  }

  public void setLevel(ErrorLevel level) {
    this.level = level;
  }

  // key
  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  /**
   * return human readable location string.
   */
  public String getLocationString() {
    if (this.location == null) {
      return null;
    }
    List<String> infoList = new LinkedList<>();
    if (this.location.getBeginLine() >= 0) {
      infoList.add(String.format("line: %d", this.location.getBeginLine()));
    }
    if (this.location.getBeginCol() >= 0) {
      infoList.add(String.format("col: %d", this.location.getBeginCol()));
    }
    if (this.location.getTriple() != null) {
      infoList.add("triple: " + this.location.getTriple().getSubject().toString()
          + " - " + this.location.getTriple().getPredicate().toString()
          + " - " + this.location.getTriple().getObject().toString());
    } else if (this.location.getNode() != null) {
      infoList.add("node: " + this.location.getNode().toString());
    }
    if (!infoList.isEmpty()) {
      return String.join(", ", infoList);
    }
    return null;
  }

  // arguments
  public Object[] getArguments() {
    return this.arguments.clone();
  }
}
