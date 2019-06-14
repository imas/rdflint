package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.ProblemLogger;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.List;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.codehaus.groovy.control.CompilerConfiguration;

public class CustomQueryValidator extends AbstractRdfValidator {

  @Override
  public void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
    if (this.getParameters().getRules() == null) {
      return;
    }
    // execute sparql & custom validation
    Graph g = Factory.createGraphMem();
    tripeSet.forEach(g::add);
    Model m = ModelFactory.createModelForGraph(g);

    this.getParameters().getRules().stream()
        .filter(r -> file.matches(r.getTarget()))
        .forEach(r -> {
          Query query = QueryFactory.create(r.getQuery());
          QueryExecution qe = QueryExecutionFactory.create(query, m);

          Binding binding = new Binding();
          binding.setVariable("rs", qe.execSelect());
          binding.setVariable("log", new ProblemLogger(problems, file, r.getName()));
          GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());
          shell.evaluate(r.getValid());
        });
  }
}