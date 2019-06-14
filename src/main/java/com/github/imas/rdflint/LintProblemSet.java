package com.github.imas.rdflint;

import com.github.imas.rdflint.LintProblem.ErrorLevel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LintProblemSet {

  private Map<String, List<LintProblem>> problemSet = new ConcurrentHashMap<>();

  public void addProblem(String fileName, LintProblem.ErrorLevel level, String message) {
    problemSet.putIfAbsent(fileName, new LinkedList<>());
    problemSet.get(fileName).add(new LintProblem(level, message));
  }

  int problemSize() {
    return problemSet.values().size();
  }

  boolean hasProblem() {
    return problemSize() > 0;
  }

  long errorSize() {
    return problemSet.values().stream()
        .mapToLong(lp -> lp.stream()
            .filter(t -> t.getLevel() == ErrorLevel.ERROR || t.getLevel() == ErrorLevel.WARN)
            .count())
        .sum();
  }

  boolean hasError() {
    return errorSize() > 0;
  }

  Map<String, List<LintProblem>> getProblemSet() {
    return problemSet;
  }

}
