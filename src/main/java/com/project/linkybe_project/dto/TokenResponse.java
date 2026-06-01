package com.project.linkybe_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 인증 성공 또는 재발급 결과로 내려줄 토큰 쌍을 담는다.
@Getter
@AllArgsConstructor
public class TokenResponse {
    // API 인증에 사용할 access token이다.
    private String accessToken;

    // access token 재발급에 사용할 refresh token이다.
    private String refreshToken;
}
