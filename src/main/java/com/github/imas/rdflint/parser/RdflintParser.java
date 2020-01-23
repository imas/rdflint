package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.validator.RdfValidator;
import java.util.List;

public interface RdflintParser {

  List<LintProblem> parse(String text);

  void addRdfValidator(RdfValidator validator);
}
