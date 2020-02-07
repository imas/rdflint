package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.jena.graph.Graph;

public abstract class RdflintParser {

  public abstract void parse(Graph g, List<LintProblem> problems);

  public static RdflintParserBuilder create() {
    return RdflintParserBuilder.create();
  }

  public static RdflintParserBuilder source(Path path) throws IOException {
    return create().source(path);
  }

  public static RdflintParserBuilder fromString(String body) {
    return create().fromString(body);
  }

}
