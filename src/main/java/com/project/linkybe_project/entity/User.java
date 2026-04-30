package com.project.linkybe_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
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
    private String kakaoId; // 카카오 고유 ID (항상 제공됨)

    @Column
    private String refreshToken; // 리프레시 토큰 저장 (탈취 방지용 DB 비교)

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
