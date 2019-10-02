package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import com.github.imas.rdflint.validator.RdfValidator;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandler;

public class RdfSyntaxValidator extends AbstractRdfValidator {

  public static class RdfLintSyntaxErrorHandler implements ErrorHandler {

    private LintProblemSet problems;
    private String filename;
    private RdfValidator validator;

    RdfLintSyntaxErrorHandler(LintProblemSet problems,
        String filename, RdfValidator validator) {
      this.problems = problems;
      this.filename = filename;
      this.validator = validator;
    }

    @Override
    public void warning(String message, long line, long col) {
      this.problems.addProblem(
          this.filename,
          new LintProblem(ErrorLevel.WARN, line, col, this.validator, "parseWarning", message));
    }

    @Override
    public void error(String message, long line, long col) {
      this.problems.addProblem(
          this.filename,
          new LintProblem(ErrorLevel.ERROR, line, col, this.validator, "parseError", message));
    }

    @Override
    public void fatal(String message, long line, long col) {
      this.error(message, line, col);
    }
  }

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
    String baseUri = this.getParameters().getBaseUri();
    Graph g = Factory.createGraphMem();
    String filename = path.substring(parentPath.length() + 1);
    String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
    RDFParser parser = RDFParser.create()
        .checking(true)
        .source(path)
        .base(baseUri + subdir)
        .errorHandler(new RdfLintSyntaxErrorHandler(problems, filename, this))
        .build();
    parser.parse(g);
    g.close();
  }

}
