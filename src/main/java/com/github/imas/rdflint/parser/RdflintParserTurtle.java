package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Factory;
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

//import com.github.imas.rdflint.LintProblemLocation;
//import com.github.imas.rdflint.validator.RdflintValidationModel;

public class RdflintParserTurtle extends AbstractRdflintParser {

  static class RdflintParseProfile extends ParserProfileStd {

    List<LintProblem> diagnosticList;
    List<RdfValidator> validationModels;

    public RdflintParseProfile(FactoryRDF factory, ErrorHandler errorHandler, IRIResolver resolver,
        PrefixMap prefixMap, Context context, boolean checking, boolean strictMode) {
      super(factory, errorHandler, resolver, prefixMap, context, checking, strictMode);
      diagnosticList = new LinkedList<>();
      validationModels = new LinkedList<>();
    }

    public List<LintProblem> getDiagnosticList() {
      return diagnosticList;
    }

    public void addValidationModel(RdfValidator m) {
      validationModels.add(m);
    }

    @Override
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
      validationModels.forEach(m -> {
        if (object.isLiteral()) {
          int length = object.getLiteralLexicalForm().length();
          List<LintProblem> diagnostic = m
              .validateTriple(subject, predicate, object,
                  (int) line, (int) col,
                  (int) line, (int) col + length);
          diagnosticList.addAll(diagnostic);
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

  @Override
  public List<LintProblem> parse(String text) {
    try {
      // validation
      FactoryRDF factory = RiotLib.factoryRDF();
      IRIResolver resolver = IRIResolver.create();
      PrefixMap prefixMap = PrefixMapFactory.createForInput();
      Context context = new Context();
      boolean checking = false;
      boolean strict = false;
      List<LintProblem> diagnosticErrorList = new LinkedList<>();
      RdflintParseProfile profile = new RdflintParseProfile(
          factory,
          new ErrorHandler() {
            private void addDiagnostic(String message, long line, long col,
                LintProblem.ErrorLevel lv) {
              diagnosticErrorList.add(new LintProblem(
                  lv,
                  null,
                  new LintProblemLocation(line, 0, line, col),
                  null, message));
            }

            @Override
            public void warning(String message, long line, long col) {
              addDiagnostic(message, line, col, ErrorLevel.WARN);
            }

            @Override
            public void error(String message, long line, long col) {
              addDiagnostic(message, line, col, ErrorLevel.ERROR);
            }

            @Override
            public void fatal(String message, long line, long col) {
              addDiagnostic(message, line, col, ErrorLevel.ERROR);
            }
          },
          resolver,
          prefixMap,
          context,
          checking,
          strict);

      this.getValidationModelList().forEach(profile::addValidationModel);

      Graph g = Factory.createGraphMem();
      ReaderRIOTFactory r = RDFParserRegistry.getFactory(Lang.TURTLE);
      ReaderRIOT reader = r.create(Lang.TURTLE, profile);
      ContentType ct = Lang.TURTLE.getContentType();

      InputStream validateIn = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
      reader.read(validateIn, null, ct, StreamRDFLib.graph(g), context);

      if (!diagnosticErrorList.isEmpty()) {
        return diagnosticErrorList;
      }
      return profile.getDiagnosticList();
    } catch (RiotParseException ex) {
      int line = (int) ex.getLine();
      int col = (int) ex.getCol();
      return Collections.singletonList(
          new LintProblem(
              LintProblem.ErrorLevel.ERROR,
              null,
              new LintProblemLocation(line, col),
              null, ex.getMessage()));
    } catch (DatatypeFormatException ex) {
      int line = 1;
      int col = 1;
      return Collections.singletonList(
          new LintProblem(
              LintProblem.ErrorLevel.ERROR,
              null,
              new LintProblemLocation(line, col),
              null, ex.getMessage()));
    } catch (Exception ex) {
      StringWriter w = new StringWriter();
      PrintWriter p = new PrintWriter(w);
      ex.printStackTrace(p);
      int line = 1;
      int col = 1;
      return Collections.singletonList(
          new LintProblem(
              LintProblem.ErrorLevel.ERROR,
              null,
              new LintProblemLocation(line, col),
              null, ex.getMessage()));
    }
  }

}
