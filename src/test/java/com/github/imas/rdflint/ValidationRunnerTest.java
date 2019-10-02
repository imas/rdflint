package com.github.imas.rdflint;

import static junit.framework.TestCase.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class ValidationRunnerTest {

  @Test
  public void defaultValidatorList() throws Exception {
    ValidationRunner runner = new ValidationRunner();
    runner.appendRdfValidatorsFromPackage("com.github.imas.rdflint.validator.impl");

    Set<String> clzSet = runner.getRdfValidators()
        .stream()
        .map(x -> x.getClass().getSimpleName())
        .collect(Collectors.toSet());

    String[] defaultValidatorNames = {
        "UndefinedSubjectValidator", "DegradeValidator",
        "TrimValidator", "ShaclValidator",
        "CustomQueryValidator", "RdfSyntaxValidator",
        "DataTypeValidator", "FileEncodingValidator",};

    for (String name : defaultValidatorNames) {
      assertTrue(name + " not found", clzSet.contains(name));
    }
  }

}
