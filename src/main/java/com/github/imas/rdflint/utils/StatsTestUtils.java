package com.github.imas.rdflint.utils;

import java.util.Arrays;

public class StatsTestUtils {

  /**
   * compute outlier values by hierarchical clustering.
   */
  public static double[] clusteringOutlierTest(double[] vals, double alpha, int targetSize) {
    if (vals.length < 3 || vals.length < targetSize) {
      return new double[]{};
    }

    Arrays.sort(vals);
    double[] spans = new double[vals.length - 1];
    for (int i = 0; i < spans.length; i++) {
      spans[i] = vals[i + 1] - vals[i];
    }
    double[] sortedSpans = Arrays.copyOf(spans, spans.length);
    Arrays.sort(sortedSpans);
    double[] maxSpans = Arrays.copyOfRange(sortedSpans, spans.length - 3, spans.length);
    if (maxSpans[1] == 0.0) {
      return new double[]{};
    }

    // test max, min
    if (maxSpans[2] == spans[0] && maxSpans[1] * alpha < maxSpans[2]) {
      return new double[]{vals[0]};
    }
    if (maxSpans[2] == spans[spans.length - 1] && maxSpans[1] * alpha < maxSpans[2]) {
      return new double[]{vals[vals.length - 1]};
    }

    // test intermediate
    double[] neighborSpan = new double[2];
    for (int i = 1; i < spans.length - 2; i++) {
      neighborSpan[0] = spans[i];
      neighborSpan[1] = spans[i + 1];
      Arrays.sort(neighborSpan);
      if (neighborSpan[0] == maxSpans[1] && neighborSpan[1] == maxSpans[2]
          && maxSpans[0] * alpha < maxSpans[1]) {
        return new double[]{vals[i + 1]};
      }
    }

    return new double[]{};
  }

}
