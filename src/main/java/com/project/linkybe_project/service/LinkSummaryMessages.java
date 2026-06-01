package com.project.linkybe_project.service;

// 링크 요약 처리에서 공통으로 사용하는 메시지를 모아둠
public final class LinkSummaryMessages {

    // 요약 생성 실패 시 사용자에게 반환할 기본 메시지
    public static final String SUMMARY_FAILED_MESSAGE = "링크 주소를 요약할 수 없습니다";

    // 제목 생성 실패 시 링크 제목에 저장할 기본 메시지
    public static final String TITLE_FAILED_MESSAGE = "제목을 생성할 수 없습니다";

    // 인스턴스 생성을 막아 상수 전용 클래스로 사용함
    private LinkSummaryMessages() {
    }
}
