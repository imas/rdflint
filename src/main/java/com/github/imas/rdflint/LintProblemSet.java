package com.github.imas.rdflint;

import com.github.imas.rdflint.LintProblem.ErrorLevel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LintProblemSet {

  private Map<String, List<LintProblem>> problemSet = new ConcurrentHashMap<>();

  /**
   * add problem and message to problem set.
   */
  public void addProblem(String fileName, LintProblem problem) {
    problemSet.putIfAbsent(fileName, new LinkedList<>());
    problemSet.get(fileName).add(problem);
  }

  public int problemSize() {
    return problemSet.values().size();
  }

  public boolean hasProblem() {
    return problemSize() > 0;
  }

  /**
   * return error size.
   */
  long errorSize(ErrorLevel minErrorLevel) {
    return problemSet.values().stream()
        .mapToLong(lp -> lp.stream()
            .filter(t -> t.getLevel().compareTo(minErrorLevel) <= 0)
            .count())
        .sum();
  }

  public boolean hasError() {
    return errorSize(ErrorLevel.WARN) > 0;
  }

  public boolean hasProblemOfLevelOrWorse(ErrorLevel minErrorLevel) {
    return errorSize(minErrorLevel) > 0;
  }

  public Map<String, List<LintProblem>> getProblemSet() {
    return problemSet;
  }

}
