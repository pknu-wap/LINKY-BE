package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.service.AuthService;
import com.project.linkybe_project.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoService kakaoService;
    private final AuthService authService;

    @GetMapping("/kakao")
    public ApiResponse<?> kakaoCallback(@RequestParam String code) {
        log.info("카카오 인가코드 수신: {}", code);

        // 1. 인가코드 → 액세스토큰
        String accessToken = kakaoService.getAccessToken(code);
        log.info("액세스토큰 발급 성공");

        // 2. 액세스토큰 → 카카오 사용자 정보 (kakaoId + 이메일)
        Map<String, String> userInfo = kakaoService.getUserInfo(accessToken);

        // 3. kakaoId 기준으로 자동 로그인/회원가입 → JWT 발급
        String token = authService.kakaoLogin(userInfo);

        return ApiResponse.success(Map.of("token", token));
    }
}
