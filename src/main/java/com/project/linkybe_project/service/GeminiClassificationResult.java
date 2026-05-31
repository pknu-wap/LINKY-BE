package com.project.linkybe_project.service;

public record GeminiClassificationResult(String category, int confidence, boolean successful) {

    public static GeminiClassificationResult success(String category, int confidence) {
        return new GeminiClassificationResult(category, confidence, true);
    }

    public static GeminiClassificationResult failure() {
        return new GeminiClassificationResult(null, 0, false);
    }
}
