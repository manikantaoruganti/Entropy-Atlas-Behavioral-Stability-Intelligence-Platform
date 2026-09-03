package com.entropyatlas.entropyatlas.utils;

/**
 * Utility class for calculating binary classification metrics.
 */
public class MetricsCalculator {

    public static class Metrics {
        public long tp;
        public long tn;
        public long fp;
        public long fn;
        public double precision;
        public double recall;
        public double f1;
        public double falsePositiveRate;
        public double falseNegativeRate;
        public double falsePositiveCost;
        public double falseNegativeCost;
        public double avgDetectionLatencyMs;
    }

    /**
     * Computes all required metrics.
     *
     * @param groundTruth array of "NORMAL" / "RISK" strings (actual labels)
     * @param predictions array of "NORMAL" / "RISK" strings (model predictions)
     * @param latencies array of detection latency in milliseconds (same length as inputs)
     * @param costFp cost per false positive
     * @param costFn cost per false negative
     * @return populated Metrics object
     */
    public static Metrics compute(String[] groundTruth, String[] predictions, long[] latencies,
                                 double costFp, double costFn) {
        Metrics m = new Metrics();
        long total = groundTruth.length;
        long latencySum = 0L;
        for (int i = 0; i < total; i++) {
            String gt = groundTruth[i];
            String pred = predictions[i];
            if (gt.equals("RISK")) {
                if (pred.equals("RISK")) {
                    m.tp++;
                } else {
                    m.fn++;
                }
            } else { // NORMAL
                if (pred.equals("NORMAL")) {
                    m.tn++;
                } else {
                    m.fp++;
                }
            }
            latencySum += latencies[i];
        }
        // rates and scores (guard against division by zero)
        m.precision = (m.tp + m.fp) > 0 ? (double) m.tp / (m.tp + m.fp) : 0.0;
        m.recall = (m.tp + m.fn) > 0 ? (double) m.tp / (m.tp + m.fn) : 0.0;
        m.f1 = (m.precision + m.recall) > 0 ? 2 * m.precision * m.recall / (m.precision + m.recall) : 0.0;
        m.falsePositiveRate = (m.fp + m.tn) > 0 ? (double) m.fp / (m.fp + m.tn) : 0.0;
        m.falseNegativeRate = (m.fn + m.tp) > 0 ? (double) m.fn / (m.fn + m.tp) : 0.0;
        m.falsePositiveCost = m.fp * costFp;
        m.falseNegativeCost = m.fn * costFn;
        m.avgDetectionLatencyMs = total > 0 ? (double) latencySum / total : 0.0;
        return m;
    }
}
