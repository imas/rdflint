package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import com.github.imas.rdflint.validator.RdfValidator;
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
          binding.setVariable("log", new ProblemLogger(this, problems, file, r.getName()));
          GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());
          shell.evaluate(r.getValid());
        });
  }

  // Problem Logger for groovy
  public static class ProblemLogger {

    private RdfValidator validator;
    private LintProblemSet set;
    private String file;
    private String name;

    /**
     * problem logger constructor.
     */
    private ProblemLogger(RdfValidator validator, LintProblemSet set, String file, String name) {
      this.validator = validator;
      this.set = set;
      this.file = file;
      this.name = name;
    }

    public void error(String msg) {
      log(ErrorLevel.ERROR, msg);
    }

    public void warn(String msg) {
      log(ErrorLevel.WARN, msg);
    }

    public void info(String msg) {
      log(ErrorLevel.INFO, msg);
    }

    private void log(ErrorLevel lv, String msg) {
      set.addProblem(this.file,
          new LintProblem(lv, this.validator, null, "customError", name + ": " + msg));
    }

  }

}