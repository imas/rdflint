package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class TrimValidator extends AbstractRdfValidator {

  @Override
  public List<LintProblem> validateTriple(Node subject, Node predicate, Node object,
      int beginLine, int beginCol, int endLine, int endCol) {
    List<LintProblem> rtn = new LinkedList<>();

    if (object.isLiteral()) {
      String s = object.getLiteralValue().toString();
      if (!s.equals(s.trim())) {
        rtn.add(new LintProblem(ErrorLevel.WARN,
            this,
            new LintProblemLocation(beginLine, beginCol, endLine, endCol,
                new Triple(subject, predicate, object)),
            "needTrimLiteral", s));
      }
    }
    return rtn;
  }

}
