package com.github.imas.rdflint;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class LintProblemLocation {

  private long beginLine;
  private long beginCol;
  private long endLine;
  private long endCol;
  private Node node;
  private Triple triple;

  private LintProblemLocation(long beginLine, long beginCol, long endLine, long endCol,
      Node node, Triple triple) {
    this.beginLine = beginLine;
    this.beginCol = beginCol;
    this.endLine = endLine;
    this.endCol = endCol;
    this.node = node;
    this.triple = triple;
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long beginLine, long beginCol, long endLine, long endCol,
      Node node) {
    this(beginLine, beginCol, endLine, endCol, node, null);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long beginLine, long beginCol, long endLine, long endCol,
      Triple triple) {
    this(beginLine, beginCol, endLine, endCol, null, triple);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long beginLine, long beginCol, long endLine, long endCol) {
    this(beginLine, beginCol, endLine, endCol, null, null);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long line, long col, Node node) {
    this(line, col, line, col, node, null);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long line, long col, Triple triple) {
    this(line, col, line, col, null, triple);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long line, long col) {
    this(line, col, line, col, null, null);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long line, Node node) {
    this(line, -1, line, -1, node, null);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long line, Triple triple) {
    this(line, -1, line, -1, null, triple);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(long line) {
    this(line, -1, line, -1, null, null);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(Node node) {
    this(-1, -1, -1, -1, node, null);
  }

  /**
   * constructor.
   */
  public LintProblemLocation(Triple triple) {
    this(-1, -1, -1, -1, null, triple);
  }


  public long getBeginLine() {
    return beginLine;
  }

  public void setBeginLine(long beginLine) {
    this.beginLine = beginLine;
  }

  public long getBeginCol() {
    return beginCol;
  }

  public void setBeginCol(long beginCol) {
    this.beginCol = beginCol;
  }

  public long getEndLine() {
    return endLine;
  }

  public void setEndLine(long endLine) {
    this.endLine = endLine;
  }

  public long getEndCol() {
    return endCol;
  }

  public void setEndCol(long endCol) {
    this.endCol = endCol;
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public Triple getTriple() {
    return triple;
  }

  public void setTriple(Triple triple) {
    this.triple = triple;
  }

}
