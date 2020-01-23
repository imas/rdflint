package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.validator.RdfValidator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractRdflintParser implements RdflintParser {

  @Override
  public abstract List<LintProblem> parse(String text);

  List<RdfValidator> validators = new LinkedList<>();

  @Override
  public void addRdfValidator(RdfValidator validator) {
    validators.add(validator);
  }

  public List<RdfValidator> getValidationModelList() {
    return validators;
  }

}
