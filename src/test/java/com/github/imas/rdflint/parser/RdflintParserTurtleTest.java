package com.github.imas.rdflint.parser;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.validator.RdfValidator;
import com.github.imas.rdflint.validator.impl.TrimValidator;
import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.junit.Test;

public class RdflintParserTurtleTest {

  @Test
  public void testValid() throws Exception {
    String rootPath = getTestRdfsPath() + "turtle/valid.ttl";

    Graph g = Factory.createGraphMem();
    List<LintProblem> problems = new LinkedList<>();
    RdflintParser.source(Paths.get(rootPath))
        .lang(Lang.TURTLE)
        .base("http://example.com/")
        .parse(g, problems);

    assertFalse(g.size() == 0);
    g.close();
    assertEquals(0, problems.size());
  }

  @Test
  public void testInvalid() throws Exception {
    String rootPath = getTestRdfsPath() + "turtle/invalid.ttl";

    Graph g = Factory.createGraphMem();
    List<LintProblem> problems = new LinkedList<>();
    RdflintParser.source(Paths.get(rootPath))
        .lang(Lang.TURTLE)
        .base("http://example.com/")
        .parse(g, problems);

    g.close();
    assertFalse(problems.size() == 0);
    assertEquals(
        "com.github.imas.rdflint.validator.impl.parseWarning",
        problems.get(0).getKey());
  }

  @Test
  public void testValidator() throws Exception {
    String rootPath = getTestRdfsPath() + "turtle/needtrim.ttl";

    List<RdfValidator> validators = new LinkedList<>();
    validators.add(new TrimValidator());
    Graph g = Factory.createGraphMem();
    List<LintProblem> problems = new LinkedList<>();
    RdflintParser.source(Paths.get(rootPath))
        .lang(Lang.TURTLE)
        .base("http://example.com/")
        .validators(validators)
        .parse(g, problems);

    assertFalse(g.size() == 0);
    g.close();
    assertFalse(problems.size() == 0);
    assertEquals(
        "com.github.imas.rdflint.validator.impl.needTrimLiteral",
        problems.get(0).getKey());
  }

  private String getTestRdfsPath() {
    URL rootUrl = this.getClass().getClassLoader().getResource("testRDFs/");
    assertNotNull("testRDFs not found", rootUrl);
    if (rootUrl.getPath().charAt(2) == ':') {
      return rootUrl.getPath().substring(1);
    }
    return rootUrl.getPath();
  }

}
