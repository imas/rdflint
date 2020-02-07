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

public class RdflintParserRdfxmlTest {

  @Test
  public void testValid() throws Exception {
    String rootPath = getTestRdfsPath() + "rdfxml/valid.rdf";

    Graph g = Factory.createGraphMem();
    List<LintProblem> problems = new LinkedList<>();
    RdflintParser.source(Paths.get(rootPath))
        .lang(Lang.RDFXML)
        .base("http://example.com/")
        .parse(g, problems);

    assertFalse(g.size() == 0);
    g.close();
    assertEquals(0, problems.size());
  }

  @Test
  public void testInvalid() throws Exception {
    String rootPath = getTestRdfsPath() + "rdfxml/invalid.rdf";

    Graph g = Factory.createGraphMem();
    List<LintProblem> problems = new LinkedList<>();
    RdflintParser.source(Paths.get(rootPath))
        .lang(Lang.RDFXML)
        .base("http://example.com/")
        .parse(g, problems);

    g.close();
    assertFalse(problems.size() == 0);
    assertEquals(
        "com.github.imas.rdflint.validator.impl.parseWarning",
        problems.get(0).getKey());
    System.out.println(problems.get(0).getArguments()[0]);
  }

  @Test
  public void testInvalidWhitespace() throws Exception {
    String rootPath = getTestRdfsPath() + "rdfxml/invalid_whitespace.rdf";

    Graph g = Factory.createGraphMem();
    List<LintProblem> problems = new LinkedList<>();
    RdflintParser.source(Paths.get(rootPath))
        .lang(Lang.RDFXML)
        .base("http://example.com/")
        .parse(g, problems);

    g.close();
    assertFalse(problems.size() == 0);
    assertEquals(
        "com.github.imas.rdflint.validator.impl.parseWarning",
        problems.get(0).getKey());
    System.out.println(problems.get(0).getArguments()[0]);
  }

  @Test
  public void testValidator() throws Exception {
    String rootPath = getTestRdfsPath() + "rdfxml/needtrim.rdf";

    List<RdfValidator> validators = new LinkedList<>();
    validators.add(new TrimValidator());
    Graph g = Factory.createGraphMem();
    List<LintProblem> problems = new LinkedList<>();
    RdflintParser.source(Paths.get(rootPath))
        .lang(Lang.RDFXML)
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
    return rootUrl.getPath();
  }

}
