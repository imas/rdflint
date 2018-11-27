package com.github.takemikami.rdflint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class LintProblemSet {

  public static int ERROR = 1;
  public static int WARNING = 2;

  private Map<String, List<LintProblem>> problemSet = new HashMap<>();

  public void addProblem(String fileName, int level, String message) {
    problemSet.putIfAbsent(fileName, new LinkedList<>());
    problemSet.get(fileName).add(new LintProblem(level, message));
  }

  public int problemSize() {
    return problemSet.values().size();
  }

  public boolean hasProblem() {
    return problemSize() > 0;
  }

  public Map<String, List<LintProblem>> getProblemSet() {
    return problemSet;
  }

  public class LintProblem {

    private int level;
    private String message;

    public LintProblem(int level, String message) {
      this.level = level;
      this.message = message;
    }

    public int getLevel() {
      return level;
    }

    public String getMessage() {
      return message;
    }
  }

}
