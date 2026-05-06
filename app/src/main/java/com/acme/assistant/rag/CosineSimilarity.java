package com.acme.assistant.rag;

public final class CosineSimilarity {

    private CosineSimilarity() {}

    public static double calculate(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    " 벡터 차원이 다릅니다: " + a.length + " != " + b.length);
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator == 0.0) {
            return 0.0;
        }

        return dotProduct / denominator;
    }
}
