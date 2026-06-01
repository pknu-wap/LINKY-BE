package com.project.linkybe_project.entity;

// 링크 요약 생성 작업의 처리 상태를 나타냄.
public enum SummaryStatus {
    // 요약 생성이 아직 시작되지 않은 상태
    PENDING,

    // 요약 생성이 진행 중인 상태
    PROCESSING,

    // 요약 생성이 정상 완료된 상태
    DONE,

    // 요약 생성에 실패한 상태
    FAILED
}
