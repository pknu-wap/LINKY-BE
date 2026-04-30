package com.project.linkybe_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    private String title;       // 팀원이 메타데이터로 채워줄 필드
    private String siteName;    // 팀원이 메타데이터로 채워줄 필드
    private String thumbnailUrl; // 팀원이 메타데이터로 채워줄 필드
    private String description;

    private String memo;        // 사용자 메모

    @Column(length = 50)
    private String category;    // 카테고리 (직접 입력)

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(columnDefinition = "TEXT")
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩을 설정
    @JoinColumn(name = "user_id")
    private User user;
}
