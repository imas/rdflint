package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.log4j.Logger;

public class UndefinedSubjectValidator extends AbstractRdfValidator {

  private static final Logger logger = Logger.getLogger(UndefinedSubjectValidator.class.getName());

  private String[] prefixes;
  private Set<String> subjects;
  private Map<String, String> resourceMap;

  /**
   * default constructor.
   */
  public UndefinedSubjectValidator() {
    this.resourceMap = new HashMap<>();
    this.resourceMap
        .put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf/org/w3/rdf-syntax-ns.ttl");
    this.resourceMap.put("http://www.w3.org/2000/01/rdf-schema#", "rdf/org/w3/rdf-schema.ttl");
    this.resourceMap.put("http://schema.org/", "rdf/org/schema/3.4/all-layers.ttl");
    this.resourceMap.put("http://xmlns.com/foaf/0.1/", "rdf/com/xmlns/foaf/20140114.rdf");
    this.resourceMap.put("http://purl.org/dc/elements/1.1/", "rdf/org/purl/dcelements.ttl");
  }

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
    // prepare subject-set of self-resource
    this.subjects = fileTripleSet.values().stream().flatMap(Collection::stream)
        .filter(t -> t.getSubject().isURI())
        .map(t -> t.getSubject().getURI())
        .collect(Collectors.toSet());

    // prepare subject-set of common-resource
    this.resourceMap.forEach((prefix, resourceName) -> {
      InputStream is = ClassLoader.getSystemResourceAsStream(resourceName);
      Graph g = Factory.createGraphMem();
      if (resourceName.endsWith("ttl")) {
        RDFParser.source(is).base(prefix).lang(Lang.TTL).parse(g);
      } else {
        RDFParser.source(is).base(prefix).lang(Lang.RDFXML).parse(g);
      }
      Set<String> sets = g.find().toList().stream()
          .map(t -> t.getSubject().getURI())
          .collect(Collectors.toSet());
      this.subjects.addAll(sets);
    });

    Set<String> prefixSet = new HashSet<>();
    if (this.getParameters().getBaseUri() != null) {
      prefixSet.add(this.getParameters().getBaseUri());
    }
    prefixSet.addAll(this.resourceMap.keySet());
    this.prefixes = prefixSet.stream().toArray(String[]::new);
  }

  @Override
  public void close() {
    this.subjects = null;
    this.resourceMap = null;
    this.prefixes = null;
  }

  @Override
  public void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
    if (logger.isTraceEnabled()) {
      logger.trace("validateTripleSet: in (file=" + file + ")");
    }
    tripeSet.forEach(t -> {
      for (Node n : new Node[]{t.getPredicate(), t.getObject()}) {
        if (n.isURI()) {
          for (String prefix : this.prefixes) {
            if (n.getURI().startsWith(prefix) && !subjects.contains(n.getURI())) {
              problems.addProblem(
                  file,
                  new LintProblem(ErrorLevel.WARN, t, this, "undefinedUri", n.getURI()) //NOPMD
              );
            }
          }
        }
      }
    });
    logger.trace("validateTripleSet: out");
  }
}
