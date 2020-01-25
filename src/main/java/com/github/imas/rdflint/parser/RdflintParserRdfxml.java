package com.github.imas.rdflint.parser;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.validator.RdfValidator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.jena.JenaRuntime;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFErrorHandler;
import org.apache.jena.rdfxml.xmlinput.ALiteral;
import org.apache.jena.rdfxml.xmlinput.ARP;
import org.apache.jena.rdfxml.xmlinput.ARPErrorNumbers;
import org.apache.jena.rdfxml.xmlinput.ARPOptions;
import org.apache.jena.rdfxml.xmlinput.AResource;
import org.apache.jena.rdfxml.xmlinput.NamespaceHandler;
import org.apache.jena.rdfxml.xmlinput.ParseException;
import org.apache.jena.rdfxml.xmlinput.StatementHandler;
import org.apache.jena.rdfxml.xmlinput.impl.ARPSaxErrorHandler;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.checker.CheckerLiterals;
import org.apache.jena.riot.lang.ReaderRIOTRDFXML;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.util.Context;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class RdflintParserRdfxml extends AbstractRdflintParser {

  String baseUri;

  public RdflintParserRdfxml(String baseUri) {
    super();
    this.baseUri = baseUri;
  }

  @Override
  public List<LintProblem> parse(String text) {
    // validation
    Graph g = Factory.createGraphMem();
    Context context = new Context();

    List<RdfValidator> models = new LinkedList<>();
    this.getValidationModelList().forEach(m -> {
      models.add(m);
    });

    List<LintProblem> diagnosticErrorList = new LinkedList<>();
    ReaderRIOTRDFXML2 reader = new ReaderRIOTRDFXML2(new ErrorHandler() {
      private void addDiagnostic(String message, long line, long col, LintProblem.ErrorLevel lv) {
        diagnosticErrorList.add(new LintProblem(
            lv,
            null,
            new LintProblemLocation(line, 1, line, col),
            null, message));
      }

      @Override
      public void warning(String message, long line, long col) {
        addDiagnostic(message, line, col, ErrorLevel.WARN);
      }

      @Override
      public void error(String message, long line, long col) {
        addDiagnostic(message, line, col, LintProblem.ErrorLevel.ERROR);
      }

      @Override
      public void fatal(String message, long line, long col) {
        addDiagnostic(message, line, col, LintProblem.ErrorLevel.ERROR);
      }
    }, models);

    ContentType ct = Lang.RDFXML.getContentType();
    InputStream validateIn = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    try {
      reader.read(validateIn, baseUri, ct, StreamRDFLib.graph(g), context);
    } catch (Exception ex) {
      // pass
    } finally {
      g.close();
    }
    if (!diagnosticErrorList.isEmpty()) {
      return diagnosticErrorList;
    }

    return reader.getDiagnosticList();
  }


  @SuppressWarnings("AbbreviationAsWord")
  static class ReaderRIOTRDFXML2 // SUPPRESS CHECKSTYLE AbbreviationAsWord
      extends ReaderRIOTRDFXML { // SUPPRESS CHECKSTYLE AbbreviationAsWord

    List<LintProblem> diagnosticList;

    public ReaderRIOTRDFXML2(ErrorHandler errorHandler, List<RdfValidator> models) {
      super(errorHandler);
      this.errorHandler = errorHandler;
      this.models = models;
      this.diagnosticList = new LinkedList<>();
    }

    private ARP arp = new ARP();
    private InputStream input = null; // NOPMD
    private Reader reader = null; // NOPMD
    private String xmlBase;
    private String filename;
    private StreamRDF sink;
    private ErrorHandler errorHandler;
    private List<RdfValidator> models;
    private Context context;

    public List<LintProblem> getDiagnosticList() {
      return this.diagnosticList;
    }

    /**
     * Sort out the base URI for RDF/XML parsing.
     */
    private static String baseURI_RDFXML( // SUPPRESS CHECKSTYLE AbbreviationAsWordInName
        String baseIRI) { // SUPPRESS CHECKSTYLE AbbreviationAsWordInName
      if (baseIRI == null) {
        return SysRIOT.chooseBaseIRI();
      } else {
        // This normalizes the URI.
        return SysRIOT.chooseBaseIRI(baseIRI);
      }
    }

    @Override
    public void read(InputStream in, String baseURI, // SUPPRESS CHECKSTYLE AbbreviationAsWordInName
        ContentType ct, StreamRDF output, Context context) {
      this.input = in;
      this.xmlBase = baseURI_RDFXML(baseURI);
      this.filename = baseURI;
      this.sink = output;
      this.context = context;
      parse();
    }

    @Override
    public void read(Reader reader, String baseURI, // SUPPRESS CHECKSTYLE AbbreviationAsWordInName
        ContentType ct, StreamRDF output, Context context) {
      this.reader = reader;
      this.xmlBase = baseURI_RDFXML(baseURI);
      this.filename = baseURI;
      this.sink = output;
      this.context = context;
      parse();
    }

    // Use with TDB requires this to be "true" - it is set by InitTDB.
    public static boolean RiotUniformCompatibility = false; // NOPMD
    // Warnings in ARP that should be errors to be compatible with
    // non-XML-based languages.  e.g. language tags should be
    // syntactically valid.
    private static int[] additionalErrors = new int[]{
        ARPErrorNumbers.WARN_MALFORMED_XMLLANG
        //, ARPErrorNumbers.WARN_MALFORMED_URI
        //, ARPErrorNumbers.WARN_STRING_NOT_NORMAL_FORM_C
    };

    // Special case of space in URI is handled in HandlerSink (below).
    // This is instead of ARPErrorNumbers.WARN_MALFORMED_URI in additionalErrors[].
    // which causes a WARN (from ARP, with line+column numbers) then a ERROR from RIOT.
    // It's a pragmatic compromise.
    private static boolean errorForSpaceInURI = true;

    // Extracted from org.apache.jena.rdfxml.xmlinput.JenaReader
    private void oneProperty(ARPOptions options, // SUPPRESS CHECKSTYLE ParameterName
        String pName, // SUPPRESS CHECKSTYLE ParameterName
        Object value) {
      if (!pName.startsWith("ERR_") && !pName.startsWith("IGN_") && !pName.startsWith("WARN_")) {
        return;
      }
      int cond = ParseException.errorCode(pName);
      if (cond == -1) {
        throw new RiotException("No such ARP property: '" + pName + "'");
      }
      int val;
      if (value instanceof String) {
        if (!((String) value).startsWith("EM_")) {
          throw new RiotException(
              "Value for ARP property does not start EM_: '" + pName + "' = '" + value + "'");
        }
        val = ParseException.errorCode((String) value);
        if (val == -1) {
          throw new RiotException(
              "Illegal value for ARP property: '" + pName + "' = '" + value + "'");
        }
      } else if (value instanceof Integer) {
        val = ((Integer) value).intValue();
        switch (val) {
          case ARPErrorNumbers.EM_IGNORE:
          case ARPErrorNumbers.EM_WARNING:
          case ARPErrorNumbers.EM_ERROR:
          case ARPErrorNumbers.EM_FATAL:
            break;
          default:
            throw new RiotException(
                "Illegal value for ARP property: '" + pName + "' = '" + value + "'");
        }
      } else {
        throw new RiotException(
            "Property \"" + pName + "\" cannot have value: " + value.toString());
      }
      options.setErrorMode(cond, val);
    }

    @Override
    public void parse() {
      // Hacked out of ARP because of all the "private" methods
      // JenaReader has reset the options since new ARP() was called.
      sink.start();
      ReaderRIOTRDFXML2.HandlerSink rslt = new ReaderRIOTRDFXML2.HandlerSink(sink, errorHandler,
          arp, models, diagnosticList);
      arp.getHandlers().setStatementHandler(rslt);
      arp.getHandlers().setErrorHandler(rslt);
      arp.getHandlers().setNamespaceHandler(rslt);

      // ARPOptions.
      ARPOptions arpOptions = arp.getOptions();
      if (RiotUniformCompatibility) {
        // Convert some warnings to errors for compatible behaviour for all parsers.
        for (int code : additionalErrors) {
          arpOptions.setErrorMode(code, ARPErrorNumbers.EM_ERROR);
        }
      }

      if (JenaRuntime.isRDF11) {
        arp.getOptions().setIRIFactory(IRIResolver.iriFactory());
      }

      if (context != null) {
        Map<String, Object> properties = null;
        try {
          @SuppressWarnings("unchecked")
          Map<String, Object> p = (Map<String, Object>) (context
              .get(SysRIOT.sysRdfReaderProperties));
          properties = p;
        } catch (Throwable ex) {
          Log.warn(this, "Problem accessing the RDF/XML reader properties: properties ignored", ex);
        }
        if (properties != null) {
          properties.forEach((k, v) -> oneProperty(arpOptions, k, v));
        }
      }
      arp.setOptionsWith(arpOptions);

      try {
        if (reader != null) {
          arp.load(reader, xmlBase);
        } else {
          arp.load(input, xmlBase);
        }
      } catch (SAXParseException e) {
        // already reported.
      } catch (IOException | SAXException | RiotParseException ex) {
        errorHandler.error(filename + ": " + ParseException.formatMessage(ex), // NOPMD
            arp.getLocator().getLineNumber(),
            arp.getLocator().getColumnNumber());
      }
      sink.finish();
    }

    private static class HandlerSink extends ARPSaxErrorHandler implements StatementHandler,
        NamespaceHandler {

      private StreamRDF output;
      private ErrorHandler riotErrorHandler;
      private CheckerLiterals checker;
      private ARP arp;
      private List<RdfValidator> models;
      private List<LintProblem> diagnosticList;

      HandlerSink(StreamRDF output, ErrorHandler errHandler, ARP arp,
          List<RdfValidator> models, List<LintProblem> diagnosticList) {
        super(new ErrorHandlerBridge(errHandler));
        this.output = output;
        this.riotErrorHandler = errHandler;
        this.checker = new CheckerLiterals(errHandler);
        this.arp = arp;
        this.models = models;
        this.diagnosticList = diagnosticList;
      }

      @Override
      public void statement(AResource subj, AResource pred, AResource obj) {
        Triple t = convert(subj, pred, obj);
        models.forEach(m -> {
          List<LintProblem> d = m.validateTriple(t.getSubject(), t.getPredicate(), t.getObject(),
              arp.getLocator().getLineNumber(), 1,
              arp.getLocator().getLineNumber(), arp.getLocator().getColumnNumber());
          diagnosticList.addAll(d);

          d = m.validateNode(t.getSubject(),
              arp.getLocator().getLineNumber(), 1,
              arp.getLocator().getLineNumber(), arp.getLocator().getColumnNumber());
          diagnosticList.addAll(d);

          d = m.validateNode(t.getPredicate(),
              arp.getLocator().getLineNumber(), 1,
              arp.getLocator().getLineNumber(), arp.getLocator().getColumnNumber());
          diagnosticList.addAll(d);

          d = m.validateNode(t.getObject(),
              arp.getLocator().getLineNumber(), 1,
              arp.getLocator().getLineNumber(), arp.getLocator().getColumnNumber());
          diagnosticList.addAll(d);

        });
        output.triple(convert(subj, pred, obj));
      }

      @Override
      public void statement(AResource subj, AResource pred, ALiteral lit) {
        Triple t = convert(subj, pred, lit);
        models.forEach(m -> {
          List<LintProblem> d = m.validateTriple(t.getSubject(), t.getPredicate(), t.getObject(),
              arp.getLocator().getLineNumber(), 1,
              arp.getLocator().getLineNumber(), arp.getLocator().getColumnNumber());
          diagnosticList.addAll(d);

          d = m.validateNode(t.getSubject(),
              arp.getLocator().getLineNumber(), 1,
              arp.getLocator().getLineNumber(), arp.getLocator().getColumnNumber());
          diagnosticList.addAll(d);

          d = m.validateNode(t.getPredicate(),
              arp.getLocator().getLineNumber(), 1,
              arp.getLocator().getLineNumber(), arp.getLocator().getColumnNumber());
          diagnosticList.addAll(d);

        });
        output.triple(convert(subj, pred, lit));
      }

      // From JenaReader
      private static Node convert(ALiteral lit) {
        String dtURI = lit.getDatatypeURI(); // SUPPRESS CHECKSTYLE AbbreviationAsWordInName
        if (dtURI == null) {
          return NodeFactory.createLiteral(lit.toString(), lit.getLang());
        }

        if (lit.isWellFormedXML()) {
          return NodeFactory.createLiteral(lit.toString(), null, true);
        }

        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtURI);
        return NodeFactory.createLiteral(lit.toString(), dt);
      }

      private Node convert(AResource r) {
        if (!r.isAnonymous()) {
          // URI.
          String uriStr = r.getURI();
          if (errorForSpaceInURI) {
            // Special check for spaces in a URI.
            // Convert to an error like TokernizerText.
            if (uriStr.contains(" ")) {
              int i = uriStr.indexOf(' ');
              String s = uriStr.substring(0, i);
              String msg = String.format("Bad character in IRI (space): <%s[space]...>", s);
              riotErrorHandler.error(msg, // NOPMD
                  arp.getLocator().getLineNumber(),
                  arp.getLocator().getColumnNumber());
              throw new RiotParseException(msg,
                  arp.getLocator().getLineNumber(),
                  arp.getLocator().getColumnNumber());
            }
          }
          return NodeFactory.createURI(uriStr);
        }

        // String id = r.getAnonymousID();
        Node rr = (Node) r.getUserData();
        if (rr == null) {
          rr = NodeFactory.createBlankNode();
          r.setUserData(rr);
        }
        return rr;
      }

      private Triple convert(AResource s, AResource p, AResource o) {
        return Triple.create(convert(s), convert(p), convert(o));
      }

      private Triple convert(AResource s, AResource p, ALiteral o) {
        Node object = convert(o);
        checker.check(object,
            arp.getLocator().getLineNumber(),
            arp.getLocator().getColumnNumber());
        return Triple.create(convert(s), convert(p), object);
      }

      @Override
      public void startPrefixMapping(String prefix, String uri) {
        output.prefix(prefix, uri);
      }

      @Override
      public void endPrefixMapping(String prefix) {
      }
    }

    private static class ErrorHandlerBridge implements RDFErrorHandler {

      private ErrorHandler errorHandler;

      ErrorHandlerBridge(ErrorHandler hander) {
        this.errorHandler = hander;
      }

      @Override
      public void warning(Exception e) {
        Pair<Integer, Integer> p = getLineCol(e);
        errorHandler.warning(e.getMessage(), p.getLeft(), p.getRight());
      }

      @Override
      public void error(Exception e) {
        Pair<Integer, Integer> p = getLineCol(e);
        errorHandler.error(e.getMessage(), p.getLeft(), p.getRight());
      }

      @Override
      public void fatalError(Exception e) {
        Pair<Integer, Integer> p = getLineCol(e);
        errorHandler.fatal(e.getMessage(), p.getLeft(), p.getRight());
      }

      private static Pair<Integer, Integer> getLineCol(Exception e) {
        if (e instanceof SAXParseException) {
          SAXParseException esax = (SAXParseException) e;
          return Pair.create(esax.getLineNumber(), esax.getColumnNumber());
        } else {
          return Pair.create(-1, -1);
        }
      }
    }

  }

}