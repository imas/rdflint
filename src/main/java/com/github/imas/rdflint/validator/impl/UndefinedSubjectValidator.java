package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.config.RdfLintParameters;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
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
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.log4j.Logger;

public class UndefinedSubjectValidator extends AbstractRdfValidator {

  private static final Logger logger = Logger.getLogger(UndefinedSubjectValidator.class.getName());

  private static Map<String, Set<String>> commonStartswithSubjectsMap = new ConcurrentHashMap<>();
  private static Map<String, Set<String>> additionalUrlSubjectsMap = new ConcurrentHashMap<>();
  private Map<String, Set<String>> additionalStartswithSubjectsMap = new ConcurrentHashMap<>();

  private String baseUri;
  private Set<String> subjects;

  // prepare subject-set of common-resource
  static {
    String[] resourceMap = {
        "http://www.w3.org/1999/02/22-rdf-syntax-ns# rdf/org/w3/rdf-syntax-ns.ttl",
        "http://www.w3.org/2000/01/rdf-schema# rdf/org/w3/rdf-schema.ttl",
        "http://www.w3.org/ns/shacl# rdf/org/w3/shacl.ttl",
        "http://schema.org/ rdf/org/schema/3.4/all-layers.ttl",
        "http://xmlns.com/foaf/0.1/ rdf/com/xmlns/foaf/20140114.rdf",
        "http://purl.org/dc/elements/1.1/ rdf/org/purl/dcelements.ttl",
    };
    for (String resourceString : resourceMap) {
      String startswith = resourceString.split(" ")[0];
      String resourceName = resourceString.split(" ")[1];

      InputStream is = ClassLoader.getSystemResourceAsStream(resourceName);
      Lang lang = resourceName.endsWith("ttl") ? Lang.TTL : Lang.RDFXML;
      Set<String> sets = loadSubjects(RDFParser.source(is), startswith, lang);
      if (sets != null) {
        commonStartswithSubjectsMap.put(startswith, sets);
      }
    }
  }

  @Override
  public void setParameters(RdfLintParameters params) {
    super.setParameters(params);

    List<Map<String, String>> paramList = getValidationParameterMapList();
    for (Map<String, String> map : paramList) {
      String url = map.get("url");
      String startswith = map.get("startswith");
      String langtype = map.get("langtype");

      // skip loaded url
      if (additionalUrlSubjectsMap.get(url) != null) {
        additionalStartswithSubjectsMap.put(startswith, additionalUrlSubjectsMap.get(url));
        continue;
      }
      Lang lang = Lang.TURTLE;
      if ("rdfxml".equalsIgnoreCase(langtype) || "rdf".equalsIgnoreCase(langtype)) {
        lang = Lang.RDFXML;
      }
      Set<String> sets = loadSubjects(RDFParser.source(url), startswith, lang);
      if (sets != null) {
        additionalStartswithSubjectsMap.put(startswith, sets);
        additionalUrlSubjectsMap.put(url, sets);
      }
    }
  }

  private static Set<String> loadSubjects(RDFParserBuilder builder, String startswith, Lang lang) {
    Graph g = Factory.createGraphMem();
    try {
      builder.base(startswith).lang(lang).parse(g);
      return g.find().toList().stream()
          .filter(t -> t.getSubject().isURI())
          .map(t -> t.getSubject().getURI())
          .collect(Collectors.toSet());
    } catch (Exception ex) {
      logger.warn(String.format("loadSubjects: skip %s", startswith));
    } finally {
      g.close();
    }
    return null;
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

    logger.trace(
        String.format("prepareValidationResource: out (subject_size=%d)", this.subjects.size()));
  }

  @Override
  public List<LintProblem> validateNode(Node node, int beginLine, int beginCol, int endLine,
      int endCol) {
    List<LintProblem> rtn = new LinkedList<>();
    boolean undefinedFlag = false;

    if (node != null && node.isURI()) {
      for (Map<String, Set<String>> map :
          Arrays.asList(commonStartswithSubjectsMap, additionalStartswithSubjectsMap)) {
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
          if (node.getURI().startsWith(entry.getKey())
              && !entry.getValue().contains(node.getURI())) {
            undefinedFlag = true;
            break;
          }
        }
      }
      if (baseUri != null
          && node.getURI().startsWith(baseUri) && !subjects.contains(node.getURI())) {
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
