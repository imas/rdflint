package com.github.imas.rdflint.validator;

import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.config.RdfLintParameters;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Triple;

public class AbstractRdfValidator implements RdfValidator {

  private RdfLintParameters params;

  @Override
  public void setParameters(RdfLintParameters params) {
    this.params = params;
  }

  public RdfLintParameters getParameters() {
    return this.params;
  }

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
  }

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
  }

  @Override
  public void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
  }

  @Override
  public void validateOriginTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
  }

  @Override
  public void close() {
  }
}
