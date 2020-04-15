package com.github.imas.rdflint.validator.impl;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;

import com.github.imas.rdflint.ConfigurationLoader;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.ValidationRunner;
import com.github.imas.rdflint.config.RdfLintParameters;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class ValidatorsTest {

  @Test
  public void commonValidatorsTest() throws Exception {
    URL rootUrl = this.getClass().getClassLoader().getResource("testValidatorsImpl/");
    assertNotNull("testValidatorsImpl not found", rootUrl);
    String rootPath = rootUrl.getPath();

    Files.walk(Paths.get(rootPath), 2)
        .filter(f -> f.toString().length() > rootPath.length())
        .filter(f -> f.toString().substring(rootPath.length()).split("/").length >= 2)
        .forEach(f -> this.executeValidatorTest(f, rootPath));
  }

  private void executeValidatorTest(Path f, String rootPath) {
    Yaml yaml = new Yaml();
    final String conf = f.toString() + "/rdflint-config.yml";
    final String suppress = f.toString() + "/rdflint-suppress.yml";
    final String expect = f.toString() + "/expected-problems.yml";
    final String[] target = f.toString().substring(rootPath.length()).split("/");
    final String targetClass = target[0];
    final String targetDataset = target[1];
    final String assertPrefix = "[" + targetClass + "/" + targetDataset + "] ";
    assertTrue(assertPrefix + conf + " not found.", new File(conf).exists());
    assertTrue(assertPrefix + expect + "not found", new File(expect).exists());

    try {
      // load rdflint-config.yml
      RdfLintParameters params = ConfigurationLoader.loadConfig(conf);
      if (new File(suppress).exists()) {
        params.setSuppressPath(suppress);
      }

      // load expected-problems.yml
      @SuppressWarnings("unchecked")
      LinkedHashMap<String, List<LinkedHashMap<String, Object>>> expectedYaml = yaml.loadAs(
          new InputStreamReader(
              Files.newInputStream(Paths.get(new File(expect).getCanonicalPath())),
              StandardCharsets.UTF_8),
          (Class<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>>)
              (Class<?>) LinkedHashMap.class
      );
      final LinkedHashMap<String, List<LinkedHashMap<String, Object>>> expected
          = expectedYaml == null ? new LinkedHashMap<>() : expectedYaml;

      // append validator
      RdfValidator v = (RdfValidator) Class
          .forName("com.github.imas.rdflint.validator.impl." + targetClass)
          .newInstance();
      ValidationRunner runner = new ValidationRunner();
      runner.appendRdfValidator(v);

      // linter process
      LintProblemSet problems = runner.execute(params, f.toString());

      // valid errors
      assertThat(assertPrefix + "problem fileset unmatched",
          problems.getProblemSet().keySet().toArray(),
          is(arrayContainingInAnyOrder(expected.keySet().toArray())));

      problems.getProblemSet().forEach((fn, lst) -> {
        String[] targetProblems = lst.stream().map(p -> {
          LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
          issue.put("key", p.getKey());
          if (p.getLocation() != null) {
            if (p.getLocation().getTriple() != null) {
              issue.put("subject", p.getLocation().getTriple().getSubject().toString());
              issue.put("predicate", p.getLocation().getTriple().getPredicate().toString());
            } else if (p.getLocation().getNode() != null) {
              issue.put("node", p.getLocation().getNode().toString());
            } else if (p.getLocation().getBeginCol() > 0) {
              issue.put("line", String.valueOf(p.getLocation().getBeginLine()));
              issue.put("column", String.valueOf(p.getLocation().getBeginCol()));
            } else if (p.getLocation().getBeginLine() > 0) {
              issue.put("line", String.valueOf(p.getLocation().getBeginLine()));
            }
          }
          return this.createKeySortedMapString(issue);
        }).toArray(String[]::new);
        String[] expectedProblems = expected.get(fn).stream()
            .map(this::createKeySortedMapString).toArray(String[]::new);
        assertThat(assertPrefix + "problem list unmatched @ " + fn,
            targetProblems,
            is(arrayContainingInAnyOrder(expectedProblems)));
      });
    } catch (IOException
        | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      fail(assertPrefix + "fail on " + e.getMessage());
    }
  }

  private String createKeySortedMapString(LinkedHashMap<String, Object> map) {
    return String.join(",",
        map.keySet().stream().sorted()
            .map(ik -> ik + "=" + map.get(ik).toString()).toArray(String[]::new));
  }

}
