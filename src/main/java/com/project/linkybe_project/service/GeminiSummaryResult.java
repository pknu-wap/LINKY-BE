package com.project.linkybe_project.service;

// Gemini 요약 생성 결과의 제목, 요약, 성공 여부를 담음
public record GeminiSummaryResult(
        // Gemini가 생성한 제목
        String title,
        // Gemini가 생성한 요약 문구
        String summary,
        // 요약 생성의 성공 여부
        boolean successful
) {

    // 성공한 요약 생성 결과를 생성
    public static GeminiSummaryResult success(String title, String summary) {
        return new GeminiSummaryResult(title, summary, true);
    }

    // 실패한 요약 생성 결과를 생성
    public static GeminiSummaryResult failure(String message) {
        return new GeminiSummaryResult(null, message, false);
    }
}
