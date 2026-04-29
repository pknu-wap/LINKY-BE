package com.project.linkybe_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String kakaoId; // 카카오 고유 ID (항상 제공됨)

    private String email;   // 이메일 (동의한 경우에만 저장, 없으면 null)
    private String provider; // 로그인 방식 ("KAKAO")

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
