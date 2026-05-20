package com.project.linkybe_project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "link",
    indexes = {
        // 유저별 링크 조회가 가장 빈번하므로 인덱스 추가
        @Index(name = "idx_link_kakao_id", columnList = "kakao_id"),
    }
)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 필수 필드 ──────────────────────────────────────
    @Column(nullable = false, length = 2048)
    private String url;

    // ── 요구사항 기본 필드 ─────────────────────────────
    private String title; // 사용자 입력 제목 (메타데이터로 보완 가능)
    private String category; // 카테고리 (예: "개발", "미분류")
    private Boolean isPrivate; // 공개(false) / 비공개(true)
    private LocalDateTime selectedDate; // 사용자가 지정한 날짜 (리마인더용)
    // ── Gemini가 생성한 요약 ─────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String summary;

    // ── 연관관계 ───────────────────────────────────────
    // FK는 users.kakao_id (unique) 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kakao_id", referencedColumnName = "kakao_id", nullable = false)
    private User user;

    @Setter(AccessLevel.NONE)
    @Column(columnDefinition = "TEXT")
    private String summary;

    public void updateSummary(String summary) {
        this.summary = summary;
    }
    @Column(name = "kakao_id", nullable = false)
    private String kakaoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kakao_id", referencedColumnName = "kakao_id",
            insertable = false, updatable = false)
    private User kakaoUser;
}
