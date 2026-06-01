package com.project.linkybe_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 저장된 링크와 요약 상태를 표현하는 JPA 엔티티임.
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "link",
        indexes = {
                @Index(name = "idx_link_device_uuid", columnList = "device_uuid"),
        }
)
public class Link {

    // 링크 테이블의 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 저장한 원본 URL
    @Column(nullable = false, length = 2048)
    private String url;

    // 링크 제목 또는 자동 생성된 제목
    private String title;

    // 링크가 속한 카테고리
    private String category;

    // 링크를 비공개로 표시할지 여부
    private Boolean isPrivate;

    // 링크와 연결된 선택 날짜 또는 리마인더 날짜
    private LocalDateTime selectedDate;

    // 링크 즐겨찾기 여부
    @Column(name = "is_favorite")
    private Boolean isFavorite;

    // Gemini 또는 사용자가 작성한 링크 요약 내용
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 요약 생성 처리 상태를 저장함.
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SummaryStatus summaryStatus = SummaryStatus.PENDING;

    // 기기별 링크 데이터를 분리하기 위한 UUID
    @Column(name = "device_uuid", nullable = false, length = 64)
    private String deviceUuid;

    // 요약 내용만 단순 갱신함
    public void updateSummary(String summary) {
        this.summary = summary;
    }

    // 저장 전 요약 상태가 비어 있으면 대기 상태로 초기화함
    @PrePersist
    public void prePersist() {
        if (summaryStatus == null) {
            summaryStatus = SummaryStatus.PENDING;
        }
    }

    // 요약 상태를 대기 중으로 변경함
    public void markSummaryPending() {
        this.summaryStatus = SummaryStatus.PENDING;
    }

    // 요약 상태를 처리 중으로 변경함
    public void markSummaryProcessing() {
        this.summaryStatus = SummaryStatus.PROCESSING;
    }

    // 요약 내용을 저장하고 상태를 완료로 변경함.
    public void markSummaryDone(String summary) {
        this.summary = summary;
        this.summaryStatus = SummaryStatus.DONE;
    }

    // 실패 메시지를 요약에 저장하고 상태를 실패로 변경함.
    public void markSummaryFailed(String message) {
        this.summary = message;
        this.summaryStatus = SummaryStatus.FAILED;
    }
}
