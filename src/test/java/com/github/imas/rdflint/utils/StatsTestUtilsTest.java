package com.github.imas.rdflint.utils;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class StatsTestUtilsTest {

  @Test
  public void clusteringTest() throws Exception {
    double[] vals1 = {133, 134, 134, 134, 135, 135, 139, 140, 140, 140, 141, 142, 142, 144, 144,
        147, 147, 149, 150, 164};
    double[] rtn1 = StatsTestUtils.clusteringOutlierTest(vals1, 1.2, 5);
    assertEquals(1, rtn1.length);
    assertEquals(164.0, rtn1[0]);

    double[] vals2 = {100, 101, 101, 102, 103, 130, 150, 151, 151, 152};
    double[] rtn2 = StatsTestUtils.clusteringOutlierTest(vals2, 1.2, 5);
    assertEquals(1, rtn2.length);
    assertEquals(130.0, rtn2[0]);

    double[] vals3 = {1};
    double[] rtn3 = StatsTestUtils.clusteringOutlierTest(vals3, 1.2, 5);
    assertEquals(0, rtn3.length);
  }

}
