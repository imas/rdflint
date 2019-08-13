package com.github.imas.rdflint.utils;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

public class StringMatchUtilsTest {

  @Test
  public void matchWildcard() throws Exception {
    assertTrue(StringMatchUtils.matchWildcard("hoge/fuga1.rdf", "*"));
    assertTrue(StringMatchUtils.matchWildcard("hoge/fuga1.rdf", "*.rdf"));
    assertTrue(StringMatchUtils.matchWildcard("hoge/fuga1.rdf", "*fuga?.rdf"));
    assertTrue(StringMatchUtils.matchWildcard("hoge/fuga$.rdf", "*fuga$.rdf"));

    assertFalse(StringMatchUtils.matchWildcard("hoge/fuga1.txt", "*.rdf"));
    assertFalse(StringMatchUtils.matchWildcard("hoge/fuga11.rdf", "*fuga?.rdf"));
    assertFalse(StringMatchUtils.matchWildcard("hoge/fuga.rdfx", "*fuga.rdf"));

    assertFalse(StringMatchUtils.matchWildcard("hoge/fuga.rdf", null));
    assertFalse(StringMatchUtils.matchWildcard(null, "*.rdf"));
  }

}
