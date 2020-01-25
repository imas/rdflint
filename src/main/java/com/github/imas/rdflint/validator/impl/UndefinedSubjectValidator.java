package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

  private static String[] commonPrefixes;
  private static Set<String> commonSubjects = new HashSet<>();
  private String baseUri;
  private Set<String> subjects;

  // prepare subject-set of common-resource
  static {
    Map<String, String> resourceMap = new ConcurrentHashMap<>();
    resourceMap
        .put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf/org/w3/rdf-syntax-ns.ttl");
    resourceMap.put("http://www.w3.org/2000/01/rdf-schema#", "rdf/org/w3/rdf-schema.ttl");
    resourceMap.put("http://schema.org/", "rdf/org/schema/3.4/all-layers.ttl");
    resourceMap.put("http://xmlns.com/foaf/0.1/", "rdf/com/xmlns/foaf/20140114.rdf");
    resourceMap.put("http://purl.org/dc/elements/1.1/", "rdf/org/purl/dcelements.ttl");

    List<String> commonPrefixesList = new LinkedList<>();
    resourceMap.forEach((prefix, resourceName) -> {
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
      commonSubjects.addAll(sets);
      commonPrefixesList.add(prefix);
    });

    commonPrefixes = resourceMap.keySet().stream().toArray(String[]::new);
  }

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
    logger.trace("prepareValidationResource: in");

    this.baseUri = this.getParameters().getBaseUri();
    this.subjects = fileTripleSet.values().stream()
        .flatMap(Collection::stream)
        .filter(t -> t.getSubject().isURI())
        .map(t -> t.getSubject().getURI())
        .collect(Collectors.toSet());

    logger.trace("prepareValidationResource: out");
  }

  @Override
  public List<LintProblem> validateNode(Node node, int beginLine, int beginCol, int endLine,
      int endCol) {
    List<LintProblem> rtn = new LinkedList<>();
    boolean undefinedFlag = false;

    if (node != null && node.isURI()) {
      for (String prefix : commonPrefixes) {
        if (node.getURI().startsWith(prefix) && !commonSubjects.contains(node.getURI())) {
          undefinedFlag = true;
          break;
        }
      }
      if (node.getURI().startsWith(baseUri) && !subjects.contains(node.getURI())) {
        undefinedFlag = true;
      }
    }

    if (undefinedFlag) {
      rtn.add(new LintProblem(ErrorLevel.WARN, this,
          new LintProblemLocation(beginLine, beginCol, endLine, endCol, node),
          "undefinedUri", node.getURI()));
    }
    return rtn;
  }
}
