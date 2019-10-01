package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class DegradeValidator extends AbstractRdfValidator {

  private Set<Triple> flatTripleSet;
  private Set<String> subjectSet;

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
    if (this.getParameters().getOriginDir() == null) {
      return;
    }

    this.flatTripleSet = fileTripleSet.values().stream().flatMap(Collection::stream)
        .collect(Collectors.toSet());
    this.subjectSet = this.flatTripleSet.stream()
        .filter(t -> t.getSubject().isURI())
        .map(t -> t.getSubject().getURI())
        .collect(Collectors.toSet());
  }

  @Override
  public void close() {
    this.flatTripleSet = null;
    this.subjectSet = null;
  }

  @Override
  public void validateOriginTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {

    // alert removed subject
    tripeSet.stream()
        .map(t -> {
          if (t.getSubject().isURI()) {
            return t.getSubject().getURI();
          }
          return null;
        })
        .distinct()
        .filter(Objects::nonNull)
        .forEach(s -> {
          if (!this.subjectSet.contains(s)) {
            problems.addProblem(file,
                new LintProblem(ErrorLevel.INFO, this, "removedSubject", s));
          }
        });

    // alert removed triple
    tripeSet.forEach(t -> {
      for (Node n : new Node[]{t.getPredicate(), t.getObject()}) {
        if (n.isURI()) {
          if (!this.flatTripleSet.contains(t)
              && this.subjectSet.contains(t.getSubject().toString())) {
            problems.addProblem(file,
                new LintProblem(ErrorLevel.INFO, this, "removedTriple", t)); //NOPMD
          }
        }
      }
    });
  }
}
