package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class TrimValidator extends AbstractRdfValidator {

  @Override
  public LintProblem validateTriple(Node subject, Node predicate, Node object,
      int beginLine, int beginCol, int endLine, int endCol) {
    if (object.isLiteral()) {
      String s = object.getLiteralValue().toString();
      if (!s.equals(s.trim())) {
        return new LintProblem(ErrorLevel.WARN,
            this,
            new LintProblemLocation(beginLine, beginCol, endLine, endCol,
                new Triple(subject, predicate, object)),
            "needTrimLiteral", s);
      }
    }
    return null;
  }

}
