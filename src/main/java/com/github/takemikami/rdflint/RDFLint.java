package com.github.takemikami.rdflint;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.yaml.snakeyaml.Yaml;


public class RDFLint {

  public static void main(String[] args) throws ParseException, IOException {

    // Parse CommandLine Parameter
    Options options = new Options();
    options.addOption("baseuri", true, "RDF base URI");
    options.addOption("targetdir", true, "Target Directory Path");
    options.addOption("config", true, "Configuration file Path");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    // Set parameter
    String baseUri = cmd.getOptionValue("baseuri");
    String parentPath = cmd.getOptionValue("targetdir");
    if (parentPath == null) {
      parentPath = ".";
    }
    String configPath = cmd.getOptionValue("config");

    // Main procedure
    RDFLint lint = new RDFLint();
    RDFLintParameters params = lint.loadConfig(configPath);
    if (baseUri != null) {
      params.setBaseUri(baseUri);
    }
    LintProblemSet problems = lint.lintRDFDataSet(params, parentPath);
    lint.printLintProblem(problems);
    if (problems.hasProblem()) {
      System.exit(1);
    }
  }

  public RDFLintParameters loadConfig(String configPath) throws IOException {
    if (configPath == null) {
      return new RDFLintParameters();
    }
    Yaml yaml = new Yaml();
    return yaml
        .loadAs(new FileReader(new File(configPath).getCanonicalPath()), RDFLintParameters.class);
  }

  public LintProblemSet lintRDFDataSet(RDFLintParameters params, String targetDir)
      throws IOException {
    LintProblemSet rtn = new LintProblemSet();
    String parentPath = new File(targetDir).getCanonicalPath();
    String baseUri = params.getBaseUri();

    // parse rdf & ttl
    Map<String, List<Triple>> fileTripleSet = Files
        .walk(Paths.get(parentPath))
        .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
        .collect(Collectors.toMap(
            e -> e.toString().substring(parentPath.length() + 1),
            e -> {
              Graph g = Factory.createGraphMem();
              String filename = e.toString().substring(parentPath.length() + 1);
              String subdir = filename.substring(0, filename.lastIndexOf("/") + 1);
              try {
                RDFParser.source(e.toString()).base(baseUri + subdir).parse(g);
              } catch (org.apache.jena.riot.RiotException ex) {
                rtn.addProblem(
                    filename,
                    LintProblemSet.ERROR,
                    ex.getMessage());
              }
              List<Triple> lst = g.find().toList();
              g.close();
              return lst;
            }
        ));

    if (rtn.hasProblem()) {
      return rtn;
    }

    if (baseUri != null) {
      // collect subjects
      Set<String> subjects = fileTripleSet.values().stream().flatMap(Collection::stream)
          .filter(t -> t.getSubject().isURI())
          .map(t -> {
            Node n = t.getSubject();
            return n.getURI();
          })
          .collect(Collectors.toSet());

      fileTripleSet.forEach((f, l) -> {
        // check undefined uri
        l.forEach(t -> {
          for (Node n : new Node[]{t.getPredicate(), t.getObject()}) {
            if (n.isURI() && n.getURI().startsWith(baseUri) && !subjects.contains(n.getURI())) {
              rtn.addProblem(
                  f,
                  LintProblemSet.WARNING,
                  "Undefined URI: " + n.getURI()
                      + " (Triple: " + t.getSubject() + " - " + t.getPredicate() + " - "
                      + t.getObject() + ")"
              );
            }
          }
        });

        // execute sparql & custom validation
        if (params.getRules() != null) {
          Graph g = Factory.createGraphMem();
          l.forEach(g::add);
          Model m = ModelFactory.createModelForGraph(g);

          params.getRules().stream()
              .filter(r -> f.matches(r.getTarget()))
              .forEach(r -> {
                Query query = QueryFactory.create(r.getQuery());
                QueryExecution qe = QueryExecutionFactory.create(query, m);

                Binding binding = new Binding();
                binding.setVariable("rs", qe.execSelect());
                binding.setVariable("log", new ProblemLogger(rtn, f, r.getName()));
                GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());
                shell.evaluate(r.getValid());
              });
        }
      });
    }

    return rtn;
  }

  // Problem Logger for groovy
  public class ProblemLogger {

    LintProblemSet set;
    String file;
    String name;

    public ProblemLogger(LintProblemSet set, String file, String name) {
      this.set = set;
      this.file = file;
      this.name = name;
    }

    public void error(String msg) {
      set.addProblem(this.file, LintProblemSet.ERROR, name + ": " + msg);
    }

    public void warn(String msg) {
      set.addProblem(this.file, LintProblemSet.WARNING, name + ": " + msg);
    }
  }

  public void printLintProblem(LintProblemSet problems) {
    problems.getProblemSet().forEach((f, l) -> {
      System.out.println(f);
      l.forEach(m -> {
        System.out.println("  " + (m.getLevel() == 1 ? "error" : "warn ") + "  " + m.getMessage());
      });
      System.out.println();
    });
  }

}