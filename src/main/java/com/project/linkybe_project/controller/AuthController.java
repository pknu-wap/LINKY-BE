package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.KakaoLoginRequest;
import com.project.linkybe_project.dto.RefreshRequest;
import com.project.linkybe_project.dto.TokenResponse;
import com.project.linkybe_project.service.AuthService;
import com.project.linkybe_project.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoService kakaoService;
    private final AuthService authService;

    // 브라우저 방식 (Authorization Code Flow)
    @GetMapping("/kakao")
    public ApiResponse<?> kakaoCallback(@RequestParam String code) {
        log.info("카카오 인가코드 수신: {}", code);

        String accessToken = kakaoService.getAccessToken(code);
        log.info("카카오 액세스토큰 발급 성공");

        Map<String, String> userInfo = kakaoService.getUserInfo(accessToken);
        TokenResponse tokens = authService.kakaoLogin(userInfo);

        return ApiResponse.success(tokens);
    }

    // 앱 방식 (Flutter 카카오 SDK가 액세스토큰 직접 전달)
    @PostMapping("/kakao")
    public ApiResponse<?> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        log.info("카카오 액세스토큰 수신");

        Map<String, String> userInfo = kakaoService.getUserInfo(request.getAccessToken());
        TokenResponse tokens = authService.kakaoLogin(userInfo);

        return ApiResponse.success(tokens);
    }

    // 액세스 토큰 재발급 (리프레시 토큰 사용)
    @PostMapping("/refresh")
    public ApiResponse<?> refresh(@RequestBody RefreshRequest request) {
        log.info("토큰 갱신 요청");

        TokenResponse tokens = authService.refresh(request.getRefreshToken());

        return ApiResponse.success(tokens);
    }

    // ───────────────────────────────────────────────
    // 회원 탈퇴 API (새로 추가됨!)
    // ───────────────────────────────────────────────
    @DeleteMapping("/withdraw")
    public ApiResponse<?> withdrawUser(Authentication authentication) {
        log.info("회원 탈퇴 요청 수신");

        // 1. 토큰을 통해 인증된 사용자의 카카오 ID 추출
        String kakaoId = (String) authentication.getPrincipal();

        // 2. 서비스의 탈퇴 로직(리프레시 토큰 삭제 및 논리적 삭제) 호출
        authService.withdrawUser(kakaoId);
        log.info("회원 탈퇴 처리 완료 - kakaoId: {}", kakaoId);

        // 3. 성공 메시지 반환
        return ApiResponse.success("회원 탈퇴가 성공적으로 처리되었습니다.");
    }
}