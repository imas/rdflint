package com.github.imas.rdflint.validator;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.config.RdfLintParameters;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public interface RdfValidator {

  void setParameters(RdfLintParameters params);

  void validateFile(LintProblemSet problems, String path, String parentPath);

  void prepareValidationResource(Map<String, List<Triple>> fileTripleSet);

  void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet);

  void validateOriginTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet);

  void reportAdditionalProblem(LintProblemSet problems);

  void close();

  LintProblem validateTriple(Node subject, Node predicate, Node object,
      int beginLine, int beginCol, int endLine, int endCol);

  LintProblem validateNode(Node node,
      int beginLine, int beginCol, int endLine, int endCol);

}
