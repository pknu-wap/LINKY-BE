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
@NoArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kakaoId;         // 카카오 고유 ID — 유저 식별 기준

    @Column(length = 512)
    private String refreshToken;    // 리프레시 토큰 (탈취 방지용 DB 보관)

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
