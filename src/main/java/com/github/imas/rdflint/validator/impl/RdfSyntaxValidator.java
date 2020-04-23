package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.parser.RdflintParser;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;

public class RdfSyntaxValidator extends AbstractRdfValidator {

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
    String baseUri = this.getParameters().getBaseUri();
    Graph g = Factory.createGraphMem();
    String filename = path.substring(parentPath.length() + 1);
    String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
    List<LintProblem> problemList = new LinkedList<>();
    try {
      RdflintParser.source(Paths.get(path))
          .base(baseUri + subdir)
          .parse(g, problemList);
    } catch (Exception ex) {
      if (problemList.isEmpty()) {
        problemList.add(new LintProblem(
            ErrorLevel.ERROR, this,
            new LintProblemLocation(1, 1),
            "parseError", ex.getMessage()));
      }
    }
    g.close();
    problemList.forEach(p -> problems.addProblem(filename, p));
  }

}
