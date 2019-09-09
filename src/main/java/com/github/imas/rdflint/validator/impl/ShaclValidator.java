package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.util.List;
import java.util.Map;
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
          .mapWith(t -> t.getSubject())
          .filterKeep(s -> result.contains(s, shaclNode("focusNode"), triple.getSubject()))
          .filterKeep(s -> result.contains(s, shaclNode("resultPath"), triple.getPredicate()))
          .toList();

      matchedResults.forEach(res -> {
        final Node detail = result
            .find(res, shaclNode("resultMessage"), Node.ANY)
            .toList().get(0).getObject();
        final Node constraint = result
            .find(res, shaclNode("sourceConstraintComponent"), Node.ANY)
            .toList().get(0).getObject();
        result.remove(res, Node.ANY, Node.ANY);
        final String msg = buildReportMessage(triple, constraint, detail);
        problems.addProblem(file, LintProblem.ErrorLevel.WARN, msg);
      });
    });
  }

  @Override
  public void reportAdditionalProblem(LintProblemSet problems) {
    result.find(Node.ANY, rdfNode("type"), shaclNode("ValidationResult"))
        .mapWith(t -> t.getSubject())
        .forEachRemaining(s -> {
          final Node subject = result
              .find(s, shaclNode("focusNode"), Node.ANY)
              .toList().get(0).getObject();
          final Node predicate = result
              .find(s, shaclNode("resultPath"), Node.ANY)
              .toList().get(0).getObject();
          final Node detail = result
              .find(s, shaclNode("resultMessage"), Node.ANY)
              .toList().get(0).getObject();
          final Node constraint = result
              .find(s, shaclNode("sourceConstraintComponent"), Node.ANY)
              .toList().get(0).getObject();
          final String msg = buildReportMessage(subject, predicate, detail, constraint);
          problems.addProblem("SHACL_Additional_Check", LintProblem.ErrorLevel.WARN, msg);
        });
  }

  private String buildReportMessage(Triple triple, Node constraint, Node detail) {
    final StringBuilder builder = new StringBuilder();
    return builder.append("(" + triple + ") ")
        .append(detail + " ")
        .append("(" + constraint + ")")
        .toString();
  }

  private String buildReportMessage(Node subject, Node predicate, Node detail, Node constraint) {
    final StringBuilder builder = new StringBuilder();
    return builder
        .append("(" + subject + " @" + predicate + " <???>) ")
        .append(detail + " ")
        .append("(" + constraint + ")")
        .toString();
  }
}
