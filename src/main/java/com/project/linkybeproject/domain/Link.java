package com.project.linkybeproject.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "links") // DB에 생성될 테이블 이름을 links로 지정합니다.
public class Link {

    @Id // 이 필드가 고유 식별자(Primary Key)임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 1, 2, 3.. 자동으로 숫자가 올라갑니다.
    private Long id;

    @Column(nullable = false, length = 500)
    private String url;

    private String title;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 데이터가 DB에 저장되기 직전에 현재 시간을 자동으로 세팅해줍니다.
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter와 Setter (기본적인 데이터 접근용)
    public Long getId() { return id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}