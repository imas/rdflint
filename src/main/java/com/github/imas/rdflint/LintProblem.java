package com.github.imas.rdflint;

import com.github.imas.rdflint.validator.RdfValidator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class LintProblem {

  public enum ErrorLevel {
    ERROR, WARN, INFO
  }

  public enum LocationType {
    GLOBAL, LINE, LINE_COL, TRIPLE, SUBJECT
  }

  private ErrorLevel level;
  private String key;
  private LocationType locType;
  private long line;
  private long col;
  private Triple triple;
  private Node subject;
  private Object[] arguments;

  /**
   * constructor for global location type.
   */
  public LintProblem(ErrorLevel level, RdfValidator validator, String key, Object... arguments) {
    String pkgName = validator.getClass().getPackage().getName();
    this.locType = LocationType.GLOBAL;
    this.level = level;
    this.key = pkgName + "." + key;
    this.arguments = arguments.clone();
  }

  /**
   * constructor for line location type.
   */
  public LintProblem(ErrorLevel level, long line,
      RdfValidator validator, String key, Object... arguments) {
    this(level, validator, key, arguments);
    this.locType = LocationType.LINE;
    this.line = line;
  }

  /**
   * constructor for line & column location type.
   */
  public LintProblem(ErrorLevel level, long line, long col,
      RdfValidator validator, String key, Object... arguments) {
    this(level, validator, key, arguments);
    this.locType = LocationType.LINE_COL;
    this.line = line;
    this.col = col;
  }

  /**
   * constructor for triple location type.
   */
  public LintProblem(ErrorLevel level, Triple triple,
      RdfValidator validator, String key, Object... arguments) {
    this(level, validator, key, arguments);
    this.locType = LocationType.TRIPLE;
    this.triple = triple;
  }

  /**
   * constructor for subject location type.
   */
  public LintProblem(ErrorLevel level, Node subject,
      RdfValidator validator, String key, Object... arguments) {
    this(level, validator, key, arguments);
    this.locType = LocationType.SUBJECT;
    this.subject = subject;
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

  // location
  public LocationType getLocType() {
    return locType;
  }

  public long getLine() {
    return this.line;
  }

  public void setLine(long line) {
    this.line = line;
  }

  public long getCol() {
    return this.col;
  }

  public void setCol(long col) {
    this.col = col;
  }

  public Triple getTriple() {
    return this.triple;
  }

  public void setTriple(Triple triple) {
    this.triple = triple;
  }

  public Node getSubject() {
    return this.subject;
  }

  public void setSubject(Node subject) {
    this.subject = subject;
  }

  /**
   * return human readable location string.
   */
  public String getLocationString() {
    switch (this.locType) {
      case LINE:
        return "line: " + this.line;
      case LINE_COL:
        return "line: " + this.line + ", col" + this.col;
      case TRIPLE:
        return "triple: " + this.triple.getSubject()
            + " - " + this.triple.getPredicate() + " - " + this.triple.getObject();
      case SUBJECT:
        return "subject: " + this.subject.toString();
      default:
        break;
    }
    return null;
  }

  // arguments
  public Object[] getArguments() {
    return this.arguments.clone();
  }
}
