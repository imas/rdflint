package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParser;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

public class ValidationRunner {

  private static final Logger logger = Logger.getLogger(ValidationRunner.class.getName());

  private List<RdfValidator> validators = new LinkedList<>();

  /**
   * append rdf validator from package.
   */
  public void appendRdfValidatorsFromPackage(String packageName) {
    Reflections reflections = new Reflections(packageName);
    reflections.getSubTypesOf(RdfValidator.class)
        .stream().filter(clz -> clz.getCanonicalName().startsWith(packageName))
        .forEach(clz -> {
          try {
            appendRdfValidator(clz.newInstance());
          } catch (InstantiationException | IllegalAccessException e) {
            logger.trace("validator append error", e);
          }
        });
  }

  /**
   * append rdf validator.
   */
  public void appendRdfValidator(RdfValidator validator) {
    validators.add(validator);
  }

  public List<RdfValidator> getRdfValidators() {
    return validators;
  }

  /**
   * execute lint process.
   */
  public LintProblemSet execute(RdfLintParameters params, String targetDir)
      throws IOException {
    logger.trace("execute: in");
    LintProblemSet rtn = new LintProblemSet();

    // initialize validators
    validators.forEach(v ->
        v.setParameters(params)
    );

    // validation: validateFile
    String parentPath = new File(targetDir).getCanonicalPath();
    Files.walk(Paths.get(parentPath))
        .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
        .forEach(f -> validators.forEach(v -> v.validateFile(rtn, f.toString(), parentPath)));
    if (rtn.hasProblem()) {
      return rtn;
    }

    // parse rdf & ttl
    String baseUri = params.getBaseUri();
    Map<String, List<Triple>> fileTripleSet = loadFileTripleSet(parentPath, baseUri);
    String originPath = params.getOriginDir() != null
        ? new File(params.getOriginDir()).getCanonicalPath() : null;
    Map<String, List<Triple>> originFileTripleSet = originPath != null
        ? loadFileTripleSet(originPath, baseUri) : new ConcurrentHashMap<>();

    // validation: validateTripleSet
    validators.forEach(v -> {
      v.prepareValidationResource(fileTripleSet);
      fileTripleSet.forEach((f, l) -> v.validateTripleSet(rtn, f, l));
      originFileTripleSet.forEach((f, l) -> v.validateOriginTripleSet(rtn, f, l));
      v.reportAdditionalProblem(rtn);
      v.close();
    });

    logger.trace("execute: out");
    return rtn;
  }

  private Map<String, List<Triple>> loadFileTripleSet(String parentPath, String baseUri)
      throws IOException {
    return Files
        .walk(Paths.get(parentPath))
        .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
        .collect(Collectors.toMap(
            e -> e.toString().substring(parentPath.length() + 1),
            e -> {
              Graph g = Factory.createGraphMem();
              String filename = e.toString().substring(parentPath.length() + 1);
              String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
              RDFParser.source(e.toString()).base(baseUri + subdir).parse(g);
              List<Triple> lst = g.find().toList();
              g.close();
              return lst;
            }
        ));
  }

}
