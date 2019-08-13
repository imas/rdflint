package com.github.imas.rdflint.utils;

public class StringMatchUtils {

  /**
   * match by wildcard.
   */
  public static boolean matchWildcard(String target, String wildcard) {
    if (target == null || wildcard == null) {
      return false;
    }
    String regex = wildcard
        .replace("\\", "\\\\")
        .replace(".", "\\.")
        .replace("^", "\\^")
        .replace("$", "\\$")
        .replace("+", "\\+")
        .replace("|", "\\|")
        .replace("*", ".*")
        .replace("?", ".");
    return target.matches(regex);
  }

}