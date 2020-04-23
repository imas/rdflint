package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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


public class RdfLint {

  public static final String VERSION = "0.1.4";
  private static final Logger logger = Logger.getLogger(RdfLint.class.getName());

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
    Map<String, String> cmdOptions = new ConcurrentHashMap<>();
    for (String key :
        Arrays.asList("targetdir", "config", "suppress", "outputdir", "baseuri", "origindir")) {
      if (cmd.hasOption(key)) {
        cmdOptions.put(key, cmd.getOptionValue(key));
      }
    }

    // Main procedure
    if (cmd.hasOption("i")) {
      // Execute Interactive mode
      InteractiveMode imode = new InteractiveMode();
      imode.execute(cmdOptions);
    } else {
      // Execute linter
      RdfLint lint = new RdfLint();
      RdfLintParameters params = ConfigurationLoader.loadParameters(cmdOptions);
      LintProblemSet problems = lint.lintRdfDataSet(params, params.getTargetDir());
      if (problems.hasProblem()) {
        Path problemsPath = Paths.get(params.getOutputDir() + "/rdflint-problems.yml");
        LintProblemFormatter.out(System.out, problems);
        LintProblemFormatter.yaml(Files.newOutputStream(problemsPath), problems);
        final String minErrorLevel = cmd.getOptionValue("minErrorLevel", "WARN");
        final LintProblem.ErrorLevel errorLevel = LintProblem.ErrorLevel.valueOf(minErrorLevel);
        if (problems.hasProblemOfLevelOrWorse(errorLevel)) {
          System.exit(1);
        }
      }
    }
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
