package com.github.imas.rdflint;

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

  private static String dumpMessage(String key, Locale locale, Object... args) {
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

  private static Object[] buildArguments(LintProblem problem) {
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
  public static void out(OutputStream out, LintProblemSet problems) {
    PrintWriter pw = FileUtils.asPrintWriterUTF8(out);
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
        map.put("locationType", m.getLocType().toString());
        switch (m.getLocType()) {
          case LINE:
            map.put("line", String.valueOf(m.getLine()));
            break;
          case LINE_COL:
            map.put("line", String.valueOf(m.getLine()));
            map.put("column", String.valueOf(m.getCol()));
            break;
          case SUBJECT:
            map.put("subject", m.getSubject().toString());
            break;
          case TRIPLE:
            map.put("subject", m.getTriple().getSubject().toString());
            map.put("predicate", m.getTriple().getPredicate().toString());
            map.put("object", m.getTriple().getObject().toString());
            break;
          default:
            break;
        }
        Object[] args = LintProblemFormatter.buildArguments(m);
        String msg = LintProblemFormatter.dumpMessage(m.getKey(), null, args);
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
