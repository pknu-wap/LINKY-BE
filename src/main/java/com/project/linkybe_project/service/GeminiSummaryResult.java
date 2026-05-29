package com.project.linkybe_project.service;

public record GeminiSummaryResult(String title, String summary, boolean successful) {

    public static GeminiSummaryResult success(String title, String summary) {
        return new GeminiSummaryResult(title, summary, true);
    }

    public static GeminiSummaryResult failure(String message) {
        return new GeminiSummaryResult(null, message, false);
    }
}
