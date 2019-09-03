package com.github.imas.rdflint.utils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import com.github.imas.rdflint.utils.DataTypeUtils.DataType;
import org.junit.Test;

public class DataTypeUtilsTest {

  @Test
  public void guessDataTypeTest() throws Exception {
    String v1 = "1";
    assertEquals(v1, DataType.NATURAL, DataTypeUtils.guessDataType(v1));

    String v2 = "-1";
    assertEquals(v2, DataType.INTEGER, DataTypeUtils.guessDataType(v2));

    String v3 = "1.0";
    assertEquals(v3, DataType.FLOAT, DataTypeUtils.guessDataType(v3));

    String v4 = "a";
    assertEquals(v4, DataType.STRING, DataTypeUtils.guessDataType(v4));
  }

  @Test
  public void isDataTypeTest() throws Exception {
    String v1 = "1";
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v1), DataType.NATURAL));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v1), DataType.INTEGER));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v1), DataType.FLOAT));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v1), DataType.STRING));

    String v2 = "-1";
    assertFalse(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v2), DataType.NATURAL));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v2), DataType.INTEGER));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v2), DataType.FLOAT));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v2), DataType.STRING));

    String v3 = "1.0";
    assertFalse(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v3), DataType.NATURAL));
    assertFalse(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v3), DataType.INTEGER));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v3), DataType.FLOAT));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v3), DataType.STRING));

    String v4 = "a";
    assertFalse(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v4), DataType.NATURAL));
    assertFalse(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v4), DataType.INTEGER));
    assertFalse(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v4), DataType.FLOAT));
    assertTrue(DataTypeUtils.isDataType(DataTypeUtils.guessDataType(v4), DataType.STRING));
  }

  @Test
  public void isLangTest() throws Exception {
    String v1 = "like";
    assertTrue(DataTypeUtils.isLang(v1, "en"));
    assertTrue(DataTypeUtils.isLang(v1, "ja"));

    String v2 = "好き♡";
    assertFalse(DataTypeUtils.isLang(v2, "en"));
    assertTrue(DataTypeUtils.isLang(v2, "ja"));
    assertFalse(DataTypeUtils.isLang(v2, "ja-kana"));
    assertFalse(DataTypeUtils.isLang(v2, "ja-hira"));

    String v3 = "すきー";
    assertFalse(DataTypeUtils.isLang(v3, "ja-kana"));
    assertTrue(DataTypeUtils.isLang(v3, "ja-hira"));
    assertTrue(DataTypeUtils.isLang(v3, "ja"));

    String v4 = "スキー";
    assertTrue(DataTypeUtils.isLang(v4, "ja-kana"));
    assertFalse(DataTypeUtils.isLang(v4, "ja-hira"));
    assertTrue(DataTypeUtils.isLang(v4, "ja"));
  }

}
