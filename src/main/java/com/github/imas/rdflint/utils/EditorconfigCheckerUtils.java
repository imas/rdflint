package com.github.imas.rdflint.utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.editorconfig.checker.util.EndOfLine;
import org.editorconfig.checker.util.IndentStyle;

public class EditorconfigCheckerUtils {

  /**
   * Wrapper of EOLValidator.
   */
  public static boolean validateEol(File f, EndOfLine eol) {
    boolean rtn = false;
    try {
      Class clazz = Class.forName("org.editorconfig.checker.validate.EOLValidator");
      Constructor cons = clazz.getDeclaredConstructor(File.class, EndOfLine.class);
      cons.setAccessible(true);
      Method method = clazz.getDeclaredMethod("validate");
      method.setAccessible(true);

      Object instance = cons.newInstance(f, eol);
      rtn = (boolean) method.invoke(instance, (Object[]) null);
    } catch (ClassNotFoundException | NoSuchMethodException
        | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      rtn = false;
    }
    return rtn;
  }

  /**
   * Wrapper of FinalNewlineValidator.
   */
  public static boolean validateFinalNewLine(File f, boolean newline, EndOfLine eol) {
    boolean rtn = false;
    try {
      Class clazz = Class.forName("org.editorconfig.checker.validate.FinalNewlineValidator");
      Constructor cons = clazz.getDeclaredConstructor(File.class, boolean.class, EndOfLine.class);
      cons.setAccessible(true);
      Method method = clazz.getDeclaredMethod("validate");
      method.setAccessible(true);

      Object instance = cons.newInstance(f, newline, eol);
      rtn = (boolean) method.invoke(instance, (Object[]) null);
    } catch (ClassNotFoundException | NoSuchMethodException
        | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      rtn = false;
    }
    return rtn;
  }

  /**
   * Wrapper of TrailingWhitespaceValidator.
   */
  public static boolean validateTrailingWhiteSpace(File f) {
    boolean rtn = false;
    try {
      Class clazz = Class.forName("org.editorconfig.checker.validate.TrailingWhitespaceValidator");
      Constructor cons = clazz.getDeclaredConstructor(File.class);
      cons.setAccessible(true);
      Method method = clazz.getDeclaredMethod("validate");
      method.setAccessible(true);

      Object instance = cons.newInstance(f);
      rtn = (boolean) method.invoke(instance, (Object[]) null);
    } catch (ClassNotFoundException | NoSuchMethodException
        | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      rtn = false;
    }
    return rtn;
  }

  /**
   * Wrapper of IndentValidator.
   */
  public static boolean validateIndent(File f, IndentStyle style, int width) {
    boolean rtn = false;
    try {
      Class clazz = Class.forName("org.editorconfig.checker.validate.IndentValidator");
      Constructor cons = clazz.getDeclaredConstructor(File.class, IndentStyle.class, int.class);
      cons.setAccessible(true);
      Method method = clazz.getDeclaredMethod("validate");
      method.setAccessible(true);

      Object instance = cons.newInstance(f, style, width);
      rtn = (boolean) method.invoke(instance, (Object[]) null);
    } catch (ClassNotFoundException | NoSuchMethodException
        | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      rtn = false;
    }
    return rtn;
  }

}
