package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import com.github.imas.rdflint.parser.RdflintParser;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.yaml.snakeyaml.Yaml;

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
    LintProblemSet problems = new LintProblemSet();

    // initialize validators
    validators.forEach(v ->
        v.setParameters(params)
    );

    // validation: validateFile
    String parentPath = new File(targetDir).getCanonicalPath();
    Files.walk(Paths.get(parentPath))
        .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
        .forEach(f -> validators.forEach(v -> v.validateFile(problems, f.toString(), parentPath)));
    if (problems.hasProblem()) {
      return problems;
    }

    // parse rdf & ttl
    String baseUri = params.getBaseUri();
    Map<String, List<Triple>> fileTripleSet = loadFileTripleSet(parentPath, baseUri);
    String originPath = params.getOriginDir() != null
        ? new File(params.getOriginDir()).getCanonicalPath() : null;
    Map<String, List<Triple>> originFileTripleSet = originPath != null
        ? loadFileTripleSet(originPath, baseUri) : new ConcurrentHashMap<>();

    // setup triple set to validator
    validators.forEach(v -> {
      v.prepareValidationResource(fileTripleSet);
    });
    // validate triple, node
    Files.walk(Paths.get(parentPath))
        .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
        .forEach(f -> {
          String filename = f.toString().substring(parentPath.length() + 1);
          String subdir = filename.substring(0, filename.lastIndexOf(File.separator) + 1);
          if (File.separatorChar == '\\') {
            subdir = filename.replaceAll("\\\\", "/");
          }
          String subBase = baseUri + subdir;

          Graph g = Factory.createGraphMem();
          List<LintProblem> fileProblems = new LinkedList<>();
          Lang lang = f.toString().endsWith(".ttl") ? Lang.TURTLE : Lang.RDFXML;
          try {
            RdflintParser.source(f)
                .lang(lang)
                .base(subBase)
                .validators(validators)
                .parse(g, fileProblems);
            logger.trace(String.format(
                "execute: Files.walk (path=%s,problemsize=%d)",
                f.toString(),
                fileProblems.size()));
          } catch (IOException ex) {
            ex.printStackTrace(); // NOPMD
          }
          String file = f.toString().substring(parentPath.length() + 1);
          fileProblems.forEach(p -> problems.addProblem(file, p));
        });

    // validation: validateTripleSet
    validators.forEach(v -> {
      fileTripleSet.forEach((f, l) -> v.validateTripleSet(problems, f, l));
      originFileTripleSet.forEach((f, l) -> v.validateOriginTripleSet(problems, f, l));
      v.reportAdditionalProblem(problems);
      v.close();
    });

    // suppress problems
    LintProblemSet filtered = suppressProblems(problems, params.getSuppressPath());

    logger.trace("execute: out");
    return filtered;
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
              String subdir = filename.substring(0, filename.lastIndexOf(File.separator) + 1);
              if (File.separatorChar == '\\') {
                subdir = filename.replaceAll("\\\\", "/");
              }
              RDFParser.source(e.toString()).base(baseUri + subdir).parse(g);
              List<Triple> lst = g.find().toList();
              g.close();
              return lst;
            }
        ));
  }

  /**
   * suppress problems.
   */
  public static LintProblemSet suppressProblems(LintProblemSet problemSet, String suppressPath)
      throws IOException {
    logger.trace("suppressProblems: in");
    if (suppressPath == null) {
      logger.trace("suppressProblems: exit");
      return problemSet;
    }
    Yaml yaml = new Yaml();
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, List<LinkedHashMap<String, Object>>> suppressYaml = yaml.loadAs(
        new InputStreamReader(
            Files.newInputStream(Paths.get(new File(suppressPath).getCanonicalPath())),
            StandardCharsets.UTF_8),
        (Class<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>>)
            (Class<?>) LinkedHashMap.class // NOPMD
    );
    final LinkedHashMap<String, List<LinkedHashMap<String, Object>>> suppress
        = suppressYaml == null ? new LinkedHashMap<>() : suppressYaml;

    LintProblemSet filtered = new LintProblemSet();
    problemSet.getProblemSet().forEach((f, l) -> {
      String relativeUriPath = f;
      if (File.separatorChar == '\\') {
        relativeUriPath = relativeUriPath.replaceAll("\\\\", "/");
      }
      List<LinkedHashMap<String, Object>> filterList = suppress.get(relativeUriPath);
      l.forEach(m -> {
        if (filterList == null) {
          filtered.addProblem(f, m);
        } else {
          boolean filter = filterList.stream().anyMatch(fm -> {
            if (m.getKey().equals(fm.get("key"))) {
              if (m.getLocation() != null) {
                if (m.getLocation().getTriple() != null
                    && m.getLocation().getTriple().getSubject().toString()
                    .equals(fm.get("subject").toString())
                    && m.getLocation().getTriple().getPredicate().toString()
                    .equals(fm.get("predicate").toString())
                    && m.getLocation().getTriple().getObject().toString()
                    .equals(fm.get("object").toString())
                ) {
                  return true;
                }
                if (m.getLocation().getNode() != null
                    && fm.get("node") != null
                    && m.getLocation().getNode().toString().equals(fm.get("node").toString())) {
                  return true;
                }
                if (m.getLocation().getBeginCol() > 0
                    && fm.get("line") != null
                    && fm.get("col") != null
                    && m.getLocation().getBeginLine() == Integer.parseInt(fm.get("line").toString())
                    && m.getLocation().getBeginCol() == Integer
                    .parseInt(fm.get("col").toString())) {
                  return true;
                }
                if (m.getLocation().getBeginLine() > 0
                    && fm.get("line") != null
                    && m.getLocation().getBeginLine() == Integer
                    .parseInt(fm.get("line").toString())) {
                  return true;
                }
              }
            }
            return false;
          });
          if (!filter) {
            filtered.addProblem(f, m);
          }
        }
      });
    });

    logger.trace("suppressProblems: out");
    return filtered;
  }

}
