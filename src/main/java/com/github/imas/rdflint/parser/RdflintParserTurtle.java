package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.ReaderRIOTFactory;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.FactoryRDF;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.ParserProfileStd;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.sparql.util.Context;
import org.apache.log4j.Logger;

public class RdflintParserTurtle extends RdflintParser {

  private static final Logger logger = Logger.getLogger(RdflintParserTurtle.class.getName());

  static class RdflintParseProfile extends ParserProfileStd {

    List<LintProblem> diagnosticList;
    List<RdfValidator> validationModels;

    public RdflintParseProfile(FactoryRDF factory, ErrorHandler errorHandler, IRIResolver resolver,
        PrefixMap prefixMap, Context context, boolean checking, boolean strictMode,
        List<RdfValidator> validationModels, List<LintProblem> diagnosticList) {
      super(factory, errorHandler, resolver, prefixMap, context, checking, strictMode);
      this.validationModels = validationModels;
      this.diagnosticList = diagnosticList;
    }

    @Override
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
      validationModels.forEach(m -> {
        if (object.isLiteral()) {
          int length = object.getLiteralLexicalForm().length();
          diagnosticList.addAll(m.validateTriple(
              subject, predicate, object,
              (int) line, (int) col, (int) line, (int) col + length));
        }
      });
      return super.createTriple(subject, predicate, object, line, col);
    }

    private String expandPrefixedName(String prefix, String localPart) {
      String expansion = getPrefixMap().expand(prefix, localPart);
      if (expansion == null) {
        if (ARQ.isTrue(ARQ.fixupUndefinedPrefixes)) {
          return RiotLib.fixupPrefixIRI(prefix, localPart);
        }
      }
      return expansion;
    }

    @Override
    public Node create(Node currentGraph, Token token) {
      int line = (int) token.getLine();
      int col = (int) token.getColumn();
      String str = token.getImage();
      validationModels.forEach(m -> {
        Node node = null;
        int length = 0;
        if (token.getType() == TokenType.PREFIXED_NAME) {
          String prefix = str;
          String suffix = token.getImage2();
          String expansion = expandPrefixedName(prefix, suffix);
          node = createURI(expansion, line, col);
          length = str.length() + suffix.length();
        } else if (token.getType() == TokenType.IRI) {
          node = createURI(str, line, col);
          length = str.length();
        }
        if (node != null) {
          List<LintProblem> diagnostic = m
              .validateNode(node, line, col, line, col + length);
          diagnosticList.addAll(diagnostic);
        }
      });
      return super.create(currentGraph, token);
    }
  }

  String text;
  List<RdfValidator> validators;

  /**
   * constructor.
   */
  public RdflintParserTurtle(String text, List<RdfValidator> validators) {
    super();
    this.text = text;
    this.validators = validators;
  }

  @Override
  public void parse(Graph g, List<LintProblem> problems) {
    List<LintProblem> diagnosticList = new LinkedList<>();
    List<LintProblem> diagnosticErrorList = new LinkedList<>();
    try {
      // validation
      FactoryRDF factory = RiotLib.factoryRDF();
      IRIResolver resolver = IRIResolver.create();
      PrefixMap prefixMap = PrefixMapFactory.createForInput();
      Context context = new Context();
      boolean checking = true;
      boolean strict = false;
      RdflintParseProfile profile = new RdflintParseProfile(
          factory,
          new RdflintParserErrorHandler(diagnosticErrorList),
          resolver,
          prefixMap,
          context,
          checking,
          strict,
          this.validators,
          diagnosticList);

      ReaderRIOTFactory r = RDFParserRegistry.getFactory(Lang.TURTLE);
      ReaderRIOT reader = r.create(Lang.TURTLE, profile);
      ContentType ct = Lang.TURTLE.getContentType();
      InputStream validateIn = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
      reader.read(validateIn, null, ct, StreamRDFLib.graph(g), context);

      if (!diagnosticErrorList.isEmpty()) {
        problems.addAll(diagnosticErrorList);
        return;
      }
      problems.addAll(diagnosticList);

    } catch (RiotParseException ex) {
      if (!diagnosticErrorList.isEmpty()) {
        problems.addAll(diagnosticErrorList);
        return;
      }
      problems.add(new LintProblem(
          LintProblem.ErrorLevel.ERROR, null,
          new LintProblemLocation((int) ex.getLine(), (int) ex.getCol()),
          null, ex.getMessage()));

    } catch (Exception ex) {
      if (!diagnosticErrorList.isEmpty()) {
        problems.addAll(diagnosticErrorList);
        return;
      }
      String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
      if (logger.isTraceEnabled()) {
        logger.trace("parse error: " + msg);
        problems.add(new LintProblem(
            LintProblem.ErrorLevel.ERROR, null,
            new LintProblemLocation(1, 1),
            null, msg));
      }
    }

  }

}
