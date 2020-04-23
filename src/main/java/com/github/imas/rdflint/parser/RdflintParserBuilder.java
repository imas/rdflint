package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;

public class RdflintParserBuilder {

  private String body;
  private String base;
  private Lang lang = Lang.RDFXML;
  private List<RdfValidator> validators;

  public static RdflintParserBuilder create() {
    return new RdflintParserBuilder();
  }

  /**
   * Build RdflintParser from path.
   */
  public RdflintParserBuilder source(Path path) throws IOException {
    this.lang = path.toString().endsWith(".ttl") ? Lang.TURTLE : Lang.RDFXML;
    this.body = Files.lines(path, StandardCharsets.UTF_8)
        .collect(Collectors.joining(System.getProperty("line.separator")));
    return this;
  }

  public RdflintParserBuilder fromString(String body) {
    this.body = body;
    return this;
  }

  public RdflintParserBuilder base(String base) {
    this.base = base;
    return this;
  }

  public RdflintParserBuilder lang(Lang lang) {
    this.lang = lang;
    return this;
  }

  public RdflintParserBuilder validators(List<RdfValidator> validators) {
    this.validators = validators;
    return this;
  }

  /**
   * Parse Source and create Graph.
   */
  public void parse(Graph g, List<LintProblem> problems) {
    this.build().parse(g, problems);
  }

  /**
   * Build RdfLintParser by lang format.
   */
  public RdflintParser build() {
    // setup validators
    if (this.validators == null) {
      this.validators = new LinkedList<>();
    }
    // build parser
    if (this.lang == Lang.RDFXML) {
      return new RdflintParserRdfxml(this.body, this.validators, this.base);
    }
    return new RdflintParserTurtle(this.body, this.validators);
  }

}
