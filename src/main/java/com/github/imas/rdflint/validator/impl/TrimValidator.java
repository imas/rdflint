package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class TrimValidator extends AbstractRdfValidator {

  @Override
  public void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
    tripeSet.forEach(t -> {
      Node n = t.getObject();
      if (n.isLiteral()) {
        String s = n.getLiteralValue().toString();
        if (!s.equals(s.trim())) {
          problems.addProblem(
              file,
              LintProblem.ErrorLevel.WARN,
              "Need Trim Literal: '" + s + "'"
                  + " (Triple: " + t.getSubject() + " - " + t.getPredicate() + " - "
                  + t.getObject() + ")"
          );
        }
      }
    });
  }

}
