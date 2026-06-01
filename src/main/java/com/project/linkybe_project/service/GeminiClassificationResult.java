package com.project.linkybe_project.service;

// Gemini 카테고리 분류 결과와 신뢰도를 담음.
public record GeminiClassificationResult(
        // 분류된 카테고리 이름
        String category,
        // 분류 결과의 신뢰도 점수
        int confidence,
        // 분류 요청의 성공 여부
        boolean successful
) {

    // 성공한 카테고리 분류 결과를 생성
    public static GeminiClassificationResult success(String category, int confidence) {
        return new GeminiClassificationResult(category, confidence, true);
    }

    // 실패한 카테고리 분류 결과를 생성
    public static GeminiClassificationResult failure() {
        return new GeminiClassificationResult(null, 0, false);
    }
}
