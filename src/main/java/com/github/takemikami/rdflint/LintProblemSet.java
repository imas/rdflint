package com.github.takemikami.rdflint;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


class LintProblemSet {

  static final int ERROR = 1;
  static final int WARNING = 2;

  private Map<String, List<LintProblem>> problemSet = new ConcurrentHashMap<>();

  void addProblem(String fileName, int level, String message) {
    problemSet.putIfAbsent(fileName, new LinkedList<>());
    problemSet.get(fileName).add(new LintProblem(level, message));
  }

  int problemSize() {
    return problemSet.values().size();
  }

  boolean hasProblem() {
    return problemSize() > 0;
  }

  Map<String, List<LintProblem>> getProblemSet() {
    return problemSet;
  }

}
