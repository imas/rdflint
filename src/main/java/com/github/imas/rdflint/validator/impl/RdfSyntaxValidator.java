package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFParser;

public class RdfSyntaxValidator extends AbstractRdfValidator {

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
    String baseUri = this.getParameters().getBaseUri();
    Graph g = Factory.createGraphMem();
    String filename = path.substring(parentPath.length() + 1);
    String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
    try {
      RDFParser.source(path).base(baseUri + subdir).parse(g);
    } catch (org.apache.jena.riot.RiotException ex) {
      problems.addProblem(
          filename,
          LintProblem.ErrorLevel.ERROR,
          ex.getMessage());
    }
    g.close();
  }

}
