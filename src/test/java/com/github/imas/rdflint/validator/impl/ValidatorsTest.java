package com.github.imas.rdflint.validator.impl;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;

import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.RdfLint;
import com.github.imas.rdflint.ValidationRunner;
import com.github.imas.rdflint.config.RdfLintParameters;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    Yaml yaml = new Yaml();

    Files.walk(Paths.get(rootPath), 2)
        .filter(f -> f.toString().length() > rootPath.length())
        .filter(f -> f.toString().substring(rootPath.length()).split("/").length >= 2)
        .forEach(f -> {
          final String conf = f.toString() + "/rdflint-config.yml";
          final String expect = f.toString() + "/expected-problems.yml";
          final String[] target = f.toString().substring(rootPath.length()).split("/");
          final String targetClass = target[0];
          final String targetDataset = target[1];
          final String assertPrefix = "[" + targetClass + "/" + targetDataset + "] ";
          assertTrue(assertPrefix + conf + " not found.", new File(conf).exists());
          assertTrue(assertPrefix + expect + "not found", new File(expect).exists());

          try {
            // load rdflint-config.yml
            RdfLint rdflint = new RdfLint();
            RdfLintParameters params = rdflint.loadConfig(conf);

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
                switch (p.getLocType()) {
                  case LINE:
                    issue.put("line", String.valueOf(p.getLine()));
                    break;
                  case LINE_COL:
                    issue.put("line", String.valueOf(p.getLine()));
                    issue.put("column", String.valueOf(p.getCol()));
                    break;
                  case SUBJECT:
                    issue.put("subject", p.getSubject().toString());
                    break;
                  case TRIPLE:
                    issue.put("subject", p.getTriple().getSubject().toString());
                    issue.put("predicate", p.getTriple().getPredicate().toString());
                    break;
                  default:
                    break;
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
        });
  }

  private String createKeySortedMapString(LinkedHashMap<String, Object> map) {
    return String.join(",",
        map.keySet().stream().sorted()
            .map(ik -> ik + "=" + map.get(ik).toString()).toArray(String[]::new));
  }

}
