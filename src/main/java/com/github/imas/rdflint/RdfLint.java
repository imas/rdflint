package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.yaml.snakeyaml.Yaml;


public class RdfLint {

  public static final String VERSION = "0.1.3";
  private static final Logger logger = Logger.getLogger(RdfLint.class.getName());

  protected static final List<String> CONFIG_SEARCH_PATH = Collections.unmodifiableList(
      new ArrayList<String>() {
        {
          add("rdflint-config.yml");
          add(".rdflint-config.yml");
          add(".rdflint/rdflint-config.yml");
          add("config/rdflint/rdflint-config.yml");
          add(".circleci/rdflint-config.yml");
        }
      }
  );

  protected static final List<String> SUPPRESS_SEARCH_PATH = Collections.unmodifiableList(
      new ArrayList<String>() {
        {
          add("rdflint-suppress.yml");
          add(".rdflint-suppress.yml");
          add(".rdflint/rdflint-suppress.yml");
          add("config/rdflint/rdflint-suppress.yml");
          add(".circleci/rdflint-suppress.yml");
        }
      }
  );

  /**
   * rdflint entry point.
   */
  public static void main(String[] args) throws ParseException, IOException {

    // Parse CommandLine Parameter
    Options options = new Options();
    options.addOption("baseuri", true, "RDF base URI");
    options.addOption("targetdir", true, "Target Directory Path");
    options.addOption("outputdir", true, "Output Directory Path");
    options.addOption("origindir", true, "Origin Dataset Directory Path");
    options.addOption("config", true, "Configuration file Path");
    options.addOption("suppress", true, "Suppress problems file Path");
    options.addOption("minErrorLevel", true,
        "Minimal logging level which is considered an error, e.g. INFO, WARN, ERROR");
    options.addOption("i", false, "Interactive mode");
    options.addOption("ls", false, "Language Server mode (experimental)");
    options.addOption("h", false, "Print usage");
    options.addOption("v", false, "Print version");
    options.addOption("vv", false, "Verbose logging (for debugging)");

    CommandLine cmd = null;

    try {
      CommandLineParser parser = new DefaultParser();
      cmd = parser.parse(options, args);
    } catch (UnrecognizedOptionException e) {
      System.out.println("Unrecognized option: " + e.getOption()); // NOPMD
      System.exit(1);
    }

    // print version
    if (cmd.hasOption("v")) {
      System.out.println("rdflint " + VERSION); // NOPMD
      return;
    }

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

    // Execute Language Server Mode
    if (cmd.hasOption("ls")) {
      RdfLintLanguageServer server = new RdfLintLanguageServer();
      Launcher<LanguageClient> launcher = LSPLauncher
          .createServerLauncher(server, System.in, System.out);
      LanguageClient client = launcher.getRemoteProxy();
      server.connect(client);
      launcher.startListening();
      return;
    }

    // Set parameter
    String targetDir = cmd.getOptionValue("targetdir");
    String parentPath = targetDir != null ? targetDir : ".";
    String configPath = cmd.getOptionValue("config");
    if (configPath == null) {
      for (String fn : CONFIG_SEARCH_PATH) {
        Path path = Paths.get(parentPath + "/" + fn);
        if (Files.exists(path)) {
          configPath = path.toAbsolutePath().toString();
          break;
        }
      }
    }
    RdfLint lint = new RdfLint();
    RdfLintParameters params = lint.loadConfig(configPath);
    setupParameters(params, cmd, targetDir, parentPath);

    // Main procedure
    if (cmd.hasOption("i")) {
      // Execute Interactive mode
      InteractiveMode imode = new InteractiveMode();
      imode.execute(params, params.getTargetDir());
    } else {
      // Execute linter
      LintProblemSet problems = lint.lintRdfDataSet(params, params.getTargetDir());
      if (problems.hasProblem()) {
        Path problemsPath = Paths.get(params.getOutputDir() + "/rdflint-problems.yml");
        LintProblemFormatter.out(System.out, problems);
        LintProblemFormatter.yaml(Files.newOutputStream(problemsPath), problems);
        final String minErrorLevel = cmd.getOptionValue("minErrorLevel","WARN");
        final LintProblem.ErrorLevel errorLevel = LintProblem.ErrorLevel.valueOf(minErrorLevel);
        if (problems.hasProblemOfLevelOrWorse(errorLevel)) {
          System.exit(1);
        }
      }
    }
  }

  static void setupParameters(
      RdfLintParameters params, CommandLine cmd, String targetDir, String parentPath) {
    String suppressPath = cmd.getOptionValue("suppress");
    if (suppressPath == null) {
      for (String fn : SUPPRESS_SEARCH_PATH) {
        Path path = Paths.get(parentPath + "/" + fn);
        if (Files.exists(path)) {
          suppressPath = path.toAbsolutePath().toString();
          break;
        }
      }
    }

    if (targetDir != null) {
      params.setTargetDir(targetDir);
    } else if (params.getTargetDir() == null) {
      params.setTargetDir(".");
    }

    String outputDir = cmd.getOptionValue("outputdir");
    if (outputDir != null) {
      params.setOutputDir(outputDir);
    } else if (params.getOutputDir() == null) {
      params.setOutputDir(params.getTargetDir());
    }

    String baseUri = cmd.getOptionValue("baseuri");
    if (baseUri != null) {
      params.setBaseUri(baseUri);
    }

    String originPath = cmd.getOptionValue("origindir");
    if (originPath != null) {
      params.setOriginDir(originPath);
    }

    if (suppressPath != null) {
      params.setSuppressPath(suppressPath);
    }
  }

  /**
   * load configuration file.
   */
  public RdfLintParameters loadConfig(String configPath) throws IOException {
    logger.trace(String.format("loadConfig: configPath=%s", configPath));
    if (configPath == null) {
      return new RdfLintParameters();
    }
    Yaml yaml = new Yaml();
    RdfLintParameters params = yaml.loadAs(
        new InputStreamReader(
            Files.newInputStream(Paths.get(new File(configPath).getCanonicalPath())),
            StandardCharsets.UTF_8),
        RdfLintParameters.class);
    return params != null ? params : new RdfLintParameters();
  }

  /**
   * rdflint main process.
   */
  LintProblemSet lintRdfDataSet(RdfLintParameters params, String targetDir)
      throws IOException {
    logger.trace("lintRdfDataSet: in");

    // execute generator
    GenerationRunner grunner = new GenerationRunner();
    grunner.execute(params, targetDir);

    // call validator runner
    ValidationRunner runner = new ValidationRunner();
    runner.appendRdfValidatorsFromPackage("com.github.imas.rdflint.validator.impl");
    return runner.execute(params, targetDir);
  }

}
