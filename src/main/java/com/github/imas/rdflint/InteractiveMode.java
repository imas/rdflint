package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EOFError;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class InteractiveMode {

  private static final Logger logger = Logger.getLogger(InteractiveMode.class.getName());
  private static ResourceBundle messages
      = ResourceBundle.getBundle("com.github.imas.rdflint.messages");

  /**
   * execute interacitve mode.
   */
  void execute(RdfLintParameters params, String targetDir) throws IOException {
    // load rdf
    Model m = DatasetLoader.loadRdfSet(params, targetDir);

    // initialize jline
    Terminal terminal = TerminalBuilder.builder()
        .system(true)
        .build();
    LineReader lineReader = LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(new InteractiveCompleter(m))
        .parser(new InteractiveParser())
        .build();

    String welcome = MessageFormat
        .format(messages.getString("interactivemode.welcome"), RdfLint.VERSION);
    System.out.println(welcome);// NOPMD

    while (true) {
      String line;

      try {
        line = lineReader.readLine("SPARQL> ");
      } catch (UserInterruptException | EndOfFileException e) {
        return;
      }

      if (!interactiveCommand(System.out, line, params, targetDir, m)) {
        return;
      }
    }
  }

  @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING")
  static boolean interactiveCommand(OutputStream out, String line,
      RdfLintParameters params, String targetDir, Model m)
      throws IOException {
    PrintWriter pw = new PrintWriter(out);

    if (StringUtils.isEmpty(line)) {
      return true;
    }

    if (line.trim().charAt(0) == ':') {
      // execute command
      String cmd = line.trim().substring(1);
      switch (cmd) {
        case "exit":
        case "quit":
          return false;

        case "check":
        case "lint":
          // execute generator
          GenerationRunner grunner = new GenerationRunner();
          grunner.execute(params, targetDir);

          // call validator runner
          ValidationRunner runner = new ValidationRunner();
          runner.appendRdfValidatorsFromPackage("com.github.imas.rdflint.validator.impl");
          LintProblemSet problems = runner.execute(params, targetDir);

          LintProblemFormatter.out(out, problems);
          break;

        case "reload":
          m.removeAll();
          m.add(DatasetLoader.loadRdfSet(params, targetDir));
          break;

        case "help":
          String helpMsg =
              Arrays.stream(new String[]{"exit", "check", "reload", "help"})
                  .map(cmdString -> ":" + cmdString + " -- "
                      + messages.getString("interactivemode.help_desc." + cmdString))
                  .collect(Collectors.joining("\n"));
          pw.println(helpMsg);
          break;

        default:
          pw.println(messages.getString("interactivemode.unknown_command"));
          break;
      }

    } else {
      // execute query
      try {
        Query query = QueryFactory.create(line);
        QueryExecution qe = QueryExecutionFactory.create(query, m);

        switch (query.getQueryType()) {
          case Query.QueryTypeSelect:
            ResultSet results = qe.execSelect();
            ResultSetFormatter.out(out, results, query);
            break;
          case Query.QueryTypeConstruct:
            Model construct = qe.execConstruct();
            RDFDataMgr.write(out, construct, RDFFormat.TURTLE_BLOCKS);
            break;
          case Query.QueryTypeDescribe:
            Model describe = qe.execDescribe();
            RDFDataMgr.write(out, describe, RDFFormat.TURTLE_BLOCKS);
            break;
          case Query.QueryTypeAsk:
            boolean bool = qe.execAsk();
            pw.println(bool);
            break;
          default:
            pw.println(messages.getString("interactivemode.unknown_querytype"));
            break;
        }
      } catch (Exception ex) {
        pw.println(ex.getLocalizedMessage());
        if (logger.isTraceEnabled()) {
          ex.printStackTrace(); // NOPMD
        }
      }
    }

    pw.flush();
    return true;
  }

  /**
   * Jline parser for interactive mode.
   */
  public static class InteractiveParser extends DefaultParser {

    /**
     * Overrided parse method. Need double return to perform command.
     */
    @Override
    public ParsedLine parse(final String line, final int cursor, ParseContext context) {
      ParsedLine pl = super.parse(line, cursor, context);

      if (context != ParseContext.ACCEPT_LINE) {
        return pl;
      }
      if (line.length() == 0) {
        throw new EOFError(-1, -1, "No command", "command");
      }
      if (!line.endsWith("\n") && line.trim().charAt(0) != ':') {
        throw new EOFError(-1, -1, "Single new line", "double newline");
      }
      return pl;
    }

  }

  /**
   * JLine completer for interactive mode.
   */
  public static class InteractiveCompleter implements Completer {

    private static final String[] COMMANDS = {
        ":exit",
        ":quit",
        ":check",
        ":lint",
        ":reload",
        ":help"
    };
    private static final String[] SPARQL_KEYWORDS = {
        "BASE",
        "PREFIX",
        "SELECT",
        "CONSTRUCT",
        "DESCRIBE",
        "ASK",
        "ORDER BY",
        "LIMIT",
        "OFFSET",
        "DISTINCT",
        "REDUCED",
        "FROM",
        "WHERE",
        "GRAPH",
        "OPTIONAL",
        "UNION",
        "FILTER",
        "STR",
        "LANG",
        "LANGMATCHES",
        "DATATYPE",
        "BOUND",
        "sameTERM",
        "isURI",
        "isIRI",
        "isLITERAL",
        "REGEX",
        "true",
        "false"
    };
    Model model;

    public InteractiveCompleter(Model model) {
      super();
      this.model = model;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
      logger.trace(line.line());

      // interactive mode command completer
      if (line.line().length() > 0 && line.line().charAt(0) == ':') {
        Stream.of(COMMANDS)
            .filter(s -> s.startsWith(line.line()))
            .forEach(s -> candidates.add(new Candidate(s)));
        return;
      }

      // prefix completer
      Map<String, String> prefixMap = this.model.getNsPrefixMap();
      int idxBefore1 = line.words().size() - 2;
      int idxBefore2 = line.words().size() - 3;
      if (idxBefore1 >= 0 && "PREFIX".equals(line.words().get(idxBefore1).toUpperCase())) {
        prefixMap.keySet().stream()
            .filter(s -> (s + ":").startsWith(line.word()))
            .sorted()
            .forEach(s -> candidates.add(new Candidate(s + ":")));
        return;
      }
      if (idxBefore2 >= 0 && "PREFIX".equals(line.words().get(idxBefore2).toUpperCase())) {
        String alias = line.words().get(idxBefore1);

        prefixMap.entrySet().stream()
            .filter(e -> alias.equals(e.getKey() + ":"))
            .map(Entry::getValue)
            .forEach(s -> candidates.add(new Candidate("<" + s + ">")));
        return;
      }

      // sparql query completer
      Stream.of(SPARQL_KEYWORDS)
          .filter(s -> s.toUpperCase().startsWith(line.word().toUpperCase()))
          .forEach(s -> candidates.add(new Candidate(s)));

      // prefix completion in query
      List<String> activePrefixList = new LinkedList<>();
      for (int i = 1; i < line.words().size(); i++) {
        if ("PREFIX".equals(line.words().get(i - 1).toUpperCase())) {
          activePrefixList.add(line.words().get(i));
        }
      }
      candidates.addAll(
          activePrefixList.stream()
              .map(s -> new Candidate(s, s, null, null, null, null, false))
              .collect(Collectors.toList())
      );
    }
  }

}
