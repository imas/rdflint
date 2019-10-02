package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.yaml.snakeyaml.Yaml;


public class RdfLint {

  private static final Logger logger = Logger.getLogger(RdfLint.class.getName());

  /**
   * rdflint entry point.
   */
  public static void main(String[] args) throws ParseException, IOException {

    // Parse CommandLine Parameter
    Options options = new Options();
    options.addOption("baseuri", true, "RDF base URI");
    options.addOption("targetdir", true, "Target Directory Path");
    options.addOption("origindir", true, "Origin Dataset Directory Path");
    options.addOption("config", true, "Configuration file Path");
    options.addOption("i", false, "Interactive mode");
    options.addOption("h", false, "Print usage");
    options.addOption("vv", false, "Verbose logging (for debugging)");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    // print usage
    if (cmd.hasOption("h")) {
      HelpFormatter f = new HelpFormatter();
      f.printHelp("rdflint [options]", options);
      return;
    }

    // verbose logging mode
    if (cmd.hasOption("vv")) {
      Logger.getLogger("com.github.imas.rdflint").setLevel(Level.TRACE);
    }

    // Set parameter
    String baseUri = cmd.getOptionValue("baseuri");
    String parentPath = cmd.getOptionValue("targetdir");
    if (parentPath == null) {
      parentPath = ".";
    }
    String originPath = cmd.getOptionValue("origindir");
    String configPath = cmd.getOptionValue("config");

    // Main procedure
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(configPath);
    params.setTargetDir(parentPath);
    if (baseUri != null) {
      params.setBaseUri(baseUri);
    }
    if (originPath != null) {
      params.setOriginDir(originPath);
    }

    if (cmd.hasOption("i")) {
      // Execute Interactive mode
      lint.interactiveMode(params, parentPath);
    } else {
      // Execute linter
      LintProblemSet problems = lint.lintRdfDataSet(params, parentPath);
      LintProblemFormatter.out(System.out, problems);
      if (problems.hasError()) {
        System.exit(1);
      }
    }
  }

  /**
   * load configuration file.
   */
  public RdfLintParameters loadConfig(String configPath) throws IOException {
    if (configPath == null) {
      return new RdfLintParameters();
    }
    Yaml yaml = new Yaml();
    return yaml.loadAs(
        new InputStreamReader(
            Files.newInputStream(Paths.get(new File(configPath).getCanonicalPath())),
            StandardCharsets.UTF_8),
        RdfLintParameters.class);
  }

  /**
   * rdflint main process.
   */
  LintProblemSet lintRdfDataSet(RdfLintParameters params, String targetDir)
      throws IOException {
    logger.trace("lintRdfDataSet: in");

    // execute generator
    generateRdfDataSet(params, targetDir);

    // call validator runner
    ValidationRunner runner = new ValidationRunner();
    runner.appendRdfValidatorsFromPackage("com.github.imas.rdflint.validator.impl");
    return runner.execute(params, targetDir);
  }


  /**
   * rdflint generation process.
   */
  void generateRdfDataSet(RdfLintParameters params, String targetDir)
      throws IOException {
    if (params.getGeneration() == null) {
      return;
    }

    // clear output
    long errSize = params.getGeneration().stream().map(g -> {
      File f = new File(targetDir + "/" + g.getOutput());
      if (!f.exists()) {
        return true;
      }
      return f.delete();
    }).filter(v -> !v).count();
    if (errSize > 0) {
      throw new IOException("rdflint generation, fail to clear existed output.");
    }

    // prepare thymeleaf template engine
    FileTemplateResolver templateResolver = new FileTemplateResolver();
    templateResolver.setTemplateMode("TEXT");
    templateResolver.setPrefix(targetDir + "/");
    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);

    // prepare rdf dataset
    Model m = this.loadRdfSet(params, targetDir);

    params.getGeneration().forEach(g -> {
      String q = g.getQuery();

      try {
        // execute query and build result set
        Query query = QueryFactory.create(q);
        QueryExecution qe = QueryExecutionFactory.create(query, m);
        ResultSet results = qe.execSelect();

        List<Map<String, String>> lst = new LinkedList<>();
        List<String> cols = new LinkedList<>();
        while (results.hasNext()) {
          QuerySolution sol = results.next();
          Iterator<String> it = sol.varNames();
          cols.clear();
          while (it.hasNext()) {
            cols.add(it.next());
          }
          lst.add(cols.stream()
              .collect(Collectors.toMap(
                  c -> c,
                  c -> sol.get(c).toString()
              )));
        }

        // apply template
        Context ctx = new Context();
        ctx.setVariable("params", params);
        ctx.setVariable("rs", lst);
        templateEngine.process(
            g.getTemplate(),
            ctx,
            Files.newBufferedWriter(Paths.get(targetDir + "/" + g.getOutput()))
        );

      } catch (Exception ex) {
        ex.printStackTrace(); // NOPMD
      }
    });
  }

  /**
   * execute interacitve mode.
   */
  void interactiveMode(RdfLintParameters params, String targetDir) throws IOException {
    // load rdf
    String parentPath = new File(targetDir).getCanonicalPath();
    Model m = this.loadRdfSet(params, targetDir);

    // initialize jline
    Terminal terminal = TerminalBuilder.builder()
        .system(true)
        .build();
    Parser p = new InteractiveParser();
    LineReader lineReader = LineReaderBuilder.builder()
        .terminal(terminal)
        .parser(p)
        .build();

    while (true) {
      String line;

      try {
        line = lineReader.readLine("SPARQL> ");
      } catch (UserInterruptException | EndOfFileException e) {
        return;
      }

      if (line.trim().charAt(0) == ':') {
        // execute command
        String cmd = line.trim().substring(1);
        switch (cmd) {
          case "exit":
          case "quit":
            return;

          case "check":
          case "lint":
            LintProblemSet problems = this.lintRdfDataSet(params, parentPath);
            LintProblemFormatter.out(System.out, problems);
            break;

          case "reload":
            m = this.loadRdfSet(params, targetDir);
            break;

          case "help":
            System.out.println(":exit -- exit interactive mode."); // NOPMD
            System.out.println(":check -- execute validation of rdflint."); // NOPMD
            System.out.println(":reload -- reload rdf dataset."); // NOPMD
            break;

          default:
            System.out.println("unknown command."); // NOPMD
            break;
        }

      } else {
        // execute query
        try {
          Query query = QueryFactory.create(line);
          QueryExecution qe = QueryExecutionFactory.create(query, m);
          ResultSet results = qe.execSelect();
          ResultSetFormatter.out(System.out, results, query);
        } catch (Exception ex) {
          ex.printStackTrace(); // NOPMD
        }
      }

    }
  }

  // create model from files (rdf, ttl)
  private Model loadRdfSet(RdfLintParameters params, String targetDir) throws IOException {
    String parentPath = new File(targetDir).getCanonicalPath();
    String baseUri = params.getBaseUri();

    Graph g = Factory.createGraphMem();
    Files.walk(Paths.get(parentPath))
        .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
        .forEach(e -> {
          Graph gf = Factory.createGraphMem();
          String filename = e.toString().substring(parentPath.length() + 1);
          String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
          RDFParser.source(e.toString()).base(baseUri + subdir).parse(gf);
          List<Triple> lst = gf.find().toList();
          gf.close();
          lst.forEach(g::add);
        });

    return ModelFactory.createModelForGraph(g);
  }
}