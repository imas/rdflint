package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class ConfigurationLoader {

  private static final Logger logger = Logger.getLogger(ConfigurationLoader.class.getName());

  protected static final List<String> CONFIG_SEARCH_PATH = Collections
      .unmodifiableList(Arrays.asList(
          "rdflint-config.yml",
          ".rdflint-config.yml",
          ".rdflint/rdflint-config.yml",
          "config/rdflint/rdflint-config.yml",
          ".circleci/rdflint-config.yml"
      ));
  protected static final List<String> SUPPRESS_SEARCH_PATH = Collections
      .unmodifiableList(Arrays.asList(
          "rdflint-suppress.yml",
          ".rdflint-suppress.yml",
          ".rdflint/rdflint-suppress.yml",
          "config/rdflint/rdflint-suppress.yml",
          ".circleci/rdflint-suppress.yml"
      ));

  // create rdflint parameters from file
  static RdfLintParameters loadParameters(Map<String, String> cmdOptions)
      throws IOException {
    // Set parameter
    String targetDir = cmdOptions.get("targetdir");
    String configPath = cmdOptions.get("config");
    String parentPath = targetDir != null ? targetDir : ".";
    if (configPath == null) {
      for (String fn : CONFIG_SEARCH_PATH) {
        Path path = Paths.get(parentPath + "/" + fn);
        if (Files.exists(path)) {
          configPath = path.toAbsolutePath().toString();
          break;
        }
      }
    }
    RdfLintParameters params = loadConfig(configPath);
    setupParameters(params, targetDir, parentPath, cmdOptions);

    return params;
  }

  static void setupParameters(
      RdfLintParameters params, String targetDir, String parentPath,
      Map<String, String> cmdOptions) {
    String suppressPath = cmdOptions.get("suppress");
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

    String outputDir = cmdOptions.get("outputdir");
    if (outputDir != null) {
      params.setOutputDir(outputDir);
    } else if (params.getOutputDir() == null) {
      params.setOutputDir(params.getTargetDir());
    }

    String baseUri = cmdOptions.get("baseuri");
    if (baseUri != null) {
      params.setBaseUri(baseUri);
    }
    String originPath = cmdOptions.get("origindir");
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
  public static RdfLintParameters loadConfig(String configPath) throws IOException {
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

}
