package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.topbraid.shacl.validation.ValidationUtil;

public class ShaclValidator extends AbstractRdfValidator {

  private Graph result;

  private Node shaclNode(String prop) {
    return NodeFactory.createURI("http://www.w3.org/ns/shacl#" + prop);
  }

  private Node rdfNode(String prop) {
    return NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#" + prop);
  }

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
    final ModelCom model = new ModelCom(new GraphMem());
    fileTripleSet.forEach((k, v) -> {
      v.forEach(t -> {
        model.add(StatementImpl.toStatement(t, model));
      });
    });
    result = ValidationUtil.validateModel(model, model, true)
        .getModel().getGraph();
  }

  @Override
  public void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
    tripeSet.forEach(triple -> {
      final List<Node> matchedResults = result
          .find(Node.ANY, rdfNode("type"), shaclNode("ValidationResult"))
          .mapWith(Triple::getSubject)
          .filterKeep(s -> result.contains(s, shaclNode("focusNode"), triple.getSubject()))
          .filterKeep(s -> result.contains(s, shaclNode("resultPath"), triple.getPredicate()))
          .filterKeep(s -> result.contains(s, shaclNode("value"), triple.getObject()))
          .toList();

      matchedResults.forEach(res -> {
        final Node detail = result
            .find(res, shaclNode("resultMessage"), Node.ANY)
            .next().getObject(); // ValidationResult has only one resultMessage
        final Node constraint = result
            .find(res, shaclNode("sourceConstraintComponent"), Node.ANY)
            .next().getObject(); // ValidationResult has only one sourceConstraintComponent
        result.remove(res, Node.ANY, Node.ANY);
        final String msg = buildReportMessage(constraint, detail);
        problems.addProblem(file,
            new LintProblem(ErrorLevel.WARN, triple, this, "shaclViolation", msg));
      });
    });
  }

  @Override
  public void reportAdditionalProblem(LintProblemSet problems) {
    result.find(Node.ANY, rdfNode("type"), shaclNode("ValidationResult"))
        .mapWith(t -> t.getSubject())
        .forEachRemaining(s -> {
          final Optional<Node> subject = result
              .find(s, shaclNode("focusNode"), Node.ANY)
              .nextOptional().map(Triple::getObject);
          final Optional<Node> predicate = result
              .find(s, shaclNode("resultPath"), Node.ANY)
              .nextOptional().map(Triple::getObject);
          final Optional<Node> object = result
              .find(s, shaclNode("value"), Node.ANY)
              .nextOptional().map(Triple::getObject);
          final Node detail = result
              .find(s, shaclNode("resultMessage"), Node.ANY)
              .next().getObject(); // ValidationResult has only one resultMessage
          final Node constraint = result
              .find(s, shaclNode("sourceConstraintComponent"), Node.ANY)
              .next().getObject(); // ValidationResult has only one sourceConstraintComponent
          final String msg = buildReportMessage(subject, predicate, object, constraint, detail);
          problems.addProblem("SHACL_Additional_Check",
              new LintProblem(ErrorLevel.WARN, this, "shaclViolation", msg));
        });
  }

  private String buildReportMessage(Node constraint, Node detail) {
    final StringBuilder builder = new StringBuilder();
    return builder
        .append(detail + " ")
        .append("(" + constraint + ")")
        .toString();
  }

  private String buildReportMessage(Optional<Node> subject, Optional<Node> predicate,
      Optional<Node> object, Node constraint, Node detail) {
    final String subjectStr = subject.map(Node::toString).orElse("???");
    final String predicateStr = predicate.map(Node::toString).orElse("???");
    final String objectStr = object.map(Node::toString).orElse("???");
    final StringBuilder builder = new StringBuilder();
    return builder
        .append(detail + " ")
        .append("(" + constraint + ")")
        .append("(triple: " + subjectStr + " - " + predicateStr + " - " + objectStr + ")")
        .toString();
  }
}
