package com.github.imas.rdflint;

import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.validator.RdfValidator;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;


public class LintProblemSet {

  private Map<String, List<LintProblem>> problemSet = new ConcurrentHashMap<>();
  private Map<String, ResourceBundle> messagesMap = new ConcurrentHashMap<>();

  /**
   * add problem and message to problem set.
   */
  public void addProblem(String fileName, LintProblem.ErrorLevel level,
      RdfValidator validator, String key, Object... arguments) {
    String pkgName = validator.getClass().getPackage().getName();
    ResourceBundle messages = messagesMap.get(pkgName);
    if (messages == null) {
      try {
        messages = ResourceBundle.getBundle(pkgName + ".messages");
        messagesMap.put(pkgName, messages);
      } catch (MissingResourceException ex) {
        // skip
      }
    }
    Object[] args = Arrays.stream(arguments).map(arg -> {
      if (arg instanceof Triple) {
        Triple t = (Triple) arg;
        return t.getSubject() + " - " + t.getPredicate() + " - " + t.getObject();
      }
      return arg;
    }).toArray();
    String msg = null;
    if (messages != null && messages.containsKey(key)) {
      msg = MessageFormat.format(messages.getString(key), args);
    } else {
      msg = StringUtils.join(args, ", ");
    }
    this.addProblem(fileName, level, msg);
  }

  public void addProblem(String fileName, LintProblem.ErrorLevel level, String message) {
    problemSet.putIfAbsent(fileName, new LinkedList<>());
    problemSet.get(fileName).add(new LintProblem(level, message));
  }

  public int problemSize() {
    return problemSet.values().size();
  }

  public boolean hasProblem() {
    return problemSize() > 0;
  }

  /**
   * return error size.
   */
  public long errorSize() {
    return problemSet.values().stream()
        .mapToLong(lp -> lp.stream()
            .filter(t -> t.getLevel() == ErrorLevel.ERROR || t.getLevel() == ErrorLevel.WARN)
            .count())
        .sum();
  }

  public boolean hasError() {
    return errorSize() > 0;
  }

  public Map<String, List<LintProblem>> getProblemSet() {
    return problemSet;
  }

}
