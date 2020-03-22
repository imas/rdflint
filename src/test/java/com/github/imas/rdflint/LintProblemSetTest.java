package com.github.imas.rdflint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.imas.rdflint.validator.RdfValidator;
import org.junit.Test;

public class LintProblemSetTest {

  @Test public void minimalErrorLevelCorrectlyUsed() {
    LintProblemSet problemSet = new LintProblemSet();
    RdfValidator v = null;
    LintProblemLocation location = null;
    String key = null;
    String fileName = "";
    problemSet.addProblem(fileName, new LintProblem(LintProblem.ErrorLevel.WARN, v,location,key));
    assertFalse(problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.ERROR));
    assertTrue(problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.WARN));

    problemSet.addProblem(fileName, new LintProblem(LintProblem.ErrorLevel.INFO, v,location,key));
    assertFalse(problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.ERROR));

    problemSet.addProblem(fileName, new LintProblem(LintProblem.ErrorLevel.ERROR, v,location,key));
    assertTrue(problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.ERROR));
  }
}