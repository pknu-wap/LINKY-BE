package com.project.linkybe_project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "link",
    indexes = {
        // 유저별 링크 조회가 가장 빈번하므로 복합 인덱스 추가
        @Index(name = "idx_link_user_id", columnList = "user_id"),
    }
)
@EntityListeners(AuditingEntityListener.class)
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

    // ── 팀원이 채워줄 메타데이터 필드 ──────────────────
    private String siteName; // 사이트명 (예: "YouTube")
    private String thumbnailUrl; // 썸네일 URL
    private String memo; // 사용자 메모

    // ── 자동 기록 ──────────────────────────────────────
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ── 연관관계 ───────────────────────────────────────
    // LAZY: 트랜잭션 안에서만 user 접근 — open-in-view: false와 안전하게 호환
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "kakao_id", nullable = false)
    private String kakaoId;
}
