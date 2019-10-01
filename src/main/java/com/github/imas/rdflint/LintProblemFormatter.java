package com.github.imas.rdflint;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.util.FileUtils;

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

}
