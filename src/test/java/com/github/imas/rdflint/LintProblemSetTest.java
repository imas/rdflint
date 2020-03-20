package com.github.imas.rdflint;

import com.github.imas.rdflint.validator.RdfValidator;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class LintProblemSetTest {

  @Test public void minimalErrorLevelCorrectlyUsed() throws Exception {
    LintProblemSet problemSet = new LintProblemSet();
    RdfValidator validator= null;
    LintProblemLocation location = null;
    String key = null;
    String fileName = "";
    problemSet.addProblem(fileName, new LintProblem(LintProblem.ErrorLevel.WARN, validator,location,key));
    assertFalse( problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.ERROR));
    assertTrue( problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.WARN));

    problemSet.addProblem(fileName, new LintProblem(LintProblem.ErrorLevel.INFO, validator,location,key));
    assertFalse( problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.ERROR));

    problemSet.addProblem(fileName, new LintProblem(LintProblem.ErrorLevel.ERROR, validator,location,key));
    assertTrue( problemSet.hasProblemOfLevelOrWorse(LintProblem.ErrorLevel.ERROR));
  }
}