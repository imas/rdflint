package com.github.imas.rdflint.utils;

import java.lang.Character.UnicodeBlock;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataTypeUtils {

  public enum DataType {
    STRING,
    FLOAT,
    INTEGER,
    NATURAL
  }

  private static final String REGEX_NATURAL = "\\d+";
  private static final String REGEX_INTEGER = "[+-]?\\d+";
  private static final String REGEX_FLOAT = "[+-]?\\d+(\\.\\d+)?";

  /**
   * guess datatype from string.
   */
  public static DataType guessDataType(String s) {
    if (s.matches(REGEX_NATURAL)) {
      return DataType.NATURAL;
    } else if (s.matches(REGEX_INTEGER)) {
      return DataType.INTEGER;
    } else if (s.matches(REGEX_FLOAT)) {
      return DataType.FLOAT;
    }
    return DataType.STRING;
  }

  /**
   * datatype check.
   */
  public static boolean isDataType(DataType t, DataType m) {
    switch (m) {
      case NATURAL:
        return t.equals(DataType.NATURAL);
      case INTEGER:
        return (t.equals(DataType.NATURAL) || t.equals(DataType.INTEGER));
      case FLOAT:
        return (t.equals(DataType.NATURAL) || t.equals(DataType.INTEGER)
            || t.equals(DataType.FLOAT));
      default:
        break;
    }
    return true;
  }

  private static Map<String, List<UnicodeBlock>> langCodeBlocks = new ConcurrentHashMap<>();
  private static List<Character> jaHiraIgnores = Arrays.asList('・', 'ー');

  static {
    langCodeBlocks.put("ja-kana", Arrays.asList(
        UnicodeBlock.KATAKANA, UnicodeBlock.MISCELLANEOUS_SYMBOLS));
    langCodeBlocks.put("ja-hira", Arrays.asList(
        UnicodeBlock.HIRAGANA, UnicodeBlock.MISCELLANEOUS_SYMBOLS));
  }

  /**
   * check language.
   */
  public static boolean isLang(String str, String lang) {
    String[] langs = lang.split("-");

    switch (langs[0]) {
      case "en":
        return str.equals(
            new String(str.getBytes(Charset.forName("US-ASCII")), Charset.forName("US-ASCII")));
      case "ja":
        if (langs.length > 1) {
          switch (langs[1]) {
            case "Kana":
            case "kana":
              for (char c : str.toCharArray()) {
                if (!langCodeBlocks.get("ja-kana").contains(Character.UnicodeBlock.of(c))) {
                  return false;
                }
              }
              return true;
            case "Hira":
            case "hira":
              for (char c : str.toCharArray()) {
                if (!langCodeBlocks.get("ja-hira").contains(Character.UnicodeBlock.of(c))
                    && !jaHiraIgnores.contains(c)) {
                  return false;
                }
              }
              return true;
            default:
              break;
          }
        }
        return true;
      default:
        break;
    }
    return true;
  }

}
