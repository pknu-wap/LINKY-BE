package com.project.linkybe_project.dto;

import lombok.Getter;
// 필요한가?
// 토큰 재발급 요청에서 refresh token 값을 담음.
@Getter
public class RefreshRequest {
    // 재발급에 사용할 refresh token
    private String refreshToken;
}
