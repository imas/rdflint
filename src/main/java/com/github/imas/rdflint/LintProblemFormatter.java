package com.github.imas.rdflint;

import com.github.imas.rdflint.LintProblem.ErrorLevel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.util.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class LintProblemFormatter {

  private static Map<String, ResourceBundle> messagesMap = new ConcurrentHashMap<>();

  /**
   * dump problem message.
   */
  public static String dumpMessage(String key, Locale locale, Object... args) {
    final String keyName = key.substring(key.lastIndexOf('.') + 1);
    final String pkgName = key.substring(0, key.lastIndexOf('.'));
    final String bundleKey = locale == null ? pkgName : pkgName + "_" + locale.toString();
    ResourceBundle messages = LintProblemFormatter.messagesMap.get(bundleKey);
    if (messages == null) {
      try {
        if (locale != null) {
          messages = ResourceBundle.getBundle(pkgName + ".messages", locale);
        } else {
          messages = ResourceBundle.getBundle(pkgName + ".messages");
        }
        LintProblemFormatter.messagesMap.put(bundleKey, messages);
      } catch (MissingResourceException ex) {
        // skip
      }
    }
    if (messages != null && messages.containsKey(keyName)) {
      return MessageFormat.format(messages.getString(keyName), args);
    }
    return StringUtils.join(args, ", ");
  }

  /**
   * build problem message arguments. 1st argument is location string.
   */
  public static Object[] buildArguments(LintProblem problem) {
    List<Object> args = new LinkedList<>();
    if (problem.getLocationString() != null) {
      args.add(problem.getLocationString());
    }
    args.addAll(Arrays.asList(problem.getArguments()));
    return args.toArray();
  }

  /**
   * dump formatted problems.
   */
  @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING")
  public static void out(OutputStream out, LintProblemSet problems) {
    PrintWriter pw = new PrintWriter(out);
    problems.getProblemSet().forEach((f, l) -> {
      pw.println(f);
      l.forEach(m -> {
        Object[] args = LintProblemFormatter.buildArguments(m);
        String msg = LintProblemFormatter.dumpMessage(m.getKey(), null, args);
        pw.println("  " + m.getLevel() + "  " + msg);
      });
      pw.println();
    });
    pw.flush();
  }

  /**
   * dump formatted problems.
   */
  @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING")
  public static void annotationGitHubAction(OutputStream out, LintProblemSet problems) {
    PrintWriter pw = new PrintWriter(out);
    problems.getProblemSet().forEach((f, l) -> {
      l.forEach(m -> {
        Object[] args = LintProblemFormatter.buildArguments(m);
        String msg = LintProblemFormatter.dumpMessage(m.getKey(), null, args);
        long lineNoBegin = 1;
        long lineNoEnd = 1;
        if (m.getLocation() != null && m.getLocation().getBeginLine() >= 0) {
          lineNoBegin = m.getLocation().getBeginLine();
          if (m.getLocation().getEndLine() >= 0) {
            lineNoEnd = m.getLocation().getEndLine();
          } else {
            lineNoEnd = lineNoBegin;
          }
        }
        pw.println(String.format(
            "::%s file=%s,line=%d,endLine=%d,title=%s#L%d::%s",
            m.getLevel() == ErrorLevel.INFO ? "warning" : "error",
            f,
            lineNoBegin,
            lineNoEnd,
            f,
            lineNoBegin,
            msg
        ));
      });
      pw.println();
    });
    pw.flush();
  }

  /**
   * dump yaml-formatted problems.
   */
  public static void yaml(OutputStream out, LintProblemSet problems) {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setIndicatorIndent(2);
    options.setIndent(4);
    Yaml yaml = new Yaml(options);

    LinkedHashMap<String, List<LinkedHashMap<String, String>>> lst = new LinkedHashMap<>();
    problems.getProblemSet().forEach((f, l) -> {
      List<LinkedHashMap<String, String>> lstmap = new LinkedList<>();
      l.forEach(m -> {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("key", m.getKey());
        map.put("level", m.getLevel().toString());
        if (m.getLocation() != null) {
          if (m.getLocation().getBeginLine() >= 0) {
            map.put("line", String.valueOf(m.getLocation().getBeginLine()));
          }
          if (m.getLocation().getBeginCol() >= 0) {
            map.put("column", String.valueOf(m.getLocation().getBeginCol()));
          }
          if (m.getLocation().getTriple() != null) {
            map.put("subject", m.getLocation().getTriple().getSubject().toString());
            map.put("predicate", m.getLocation().getTriple().getPredicate().toString());
            map.put("object", m.getLocation().getTriple().getObject().toString());
          } else if (m.getLocation().getNode() != null) {
            map.put("node", m.getLocation().getNode().toString());
          }
        }

        Object[] args = LintProblemFormatter.buildArguments(m);
        String msg = LintProblemFormatter.dumpMessage(m.getKey(), Locale.ROOT, args);
        map.put("message", msg);
        lstmap.add(map);
      });
      lst.put(f, lstmap);
    });
    String s = yaml.dump(lst);
    PrintWriter pw = FileUtils.asPrintWriterUTF8(out);
    pw.print(s);
    pw.flush();
  }

}
