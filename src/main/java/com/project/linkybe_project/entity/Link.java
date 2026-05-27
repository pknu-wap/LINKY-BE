package com.project.linkybe_project.entity;

import jakarta.persistence.*;
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
    private LocalDateTime createdAt;

    // ── Gemini가 생성한 요약 ─────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String summary;

    // ── 연관관계 ───────────────────────────────────────
    // FK는 users.kakao_id (unique) 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kakao_id", referencedColumnName = "kakao_id", nullable = false)
    private User user;

    @Column(name = "kakao_id", nullable = false, insertable = false, updatable = false)
    private String kakaoId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "kakao_id",
//            insertable = false, updatable = false)
//    private User kakaoUser;

    public void updateSummary(String summary) {
        this.summary = summary;
    }

    // AI가 카테고리를 업데이트할 때 사용하는 메서드
    // "즐겨찾기"는 사용자만 설정할 수 있으므로 AI는 절대 지정할 수 없도록 막음
    public void updateCategory(String category) {
        if ("즐겨찾기".equals(category)) {
            // 혹시 AI가 "즐겨찾기"를 반환해도 엔티티 레벨에서 차단
            this.category = "전체";
            return;
        }
        // 기존에 사용자가 직접 지정한 카테고리가 있으면 덮어쓰지 않음
        // (사용자가 "즐겨찾기"로 저장한 경우 AI가 바꾸면 안 됨)
        if ("즐겨찾기".equals(this.category)) {
            return;
        }
        this.category = category;
    }
}