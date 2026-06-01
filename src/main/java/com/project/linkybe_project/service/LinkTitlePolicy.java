package com.project.linkybe_project.service;

// 링크 제목을 자동 생성 결과로 교체할 수 있는지 판단함
public final class LinkTitlePolicy {

    // 기본 제목이 비어 있거나 임시 제목이면 교체 대상
    public static boolean shouldReplaceTitle(String title) {
        return title == null || title.isBlank() || "요약중입니다...".equals(title.trim());
    }

    // 인스턴스 생성을 막아 정책 메서드 전용 클래스로 사용
    private LinkTitlePolicy() {
    }
}
