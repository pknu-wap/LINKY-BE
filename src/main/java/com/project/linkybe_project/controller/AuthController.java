    package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.service.AuthService;
import com.project.linkybe_project.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoService kakaoService;
    private final AuthService authService;

    @PostMapping("/kakao")
    public ApiResponse<?> kakaoLogin(@RequestBody Map<String, String> body) {

        String accessToken = body.get("accessToken");

        String email = kakaoService.getEmail(accessToken);

        String token = authService.kakaoLogin(email);

        return ApiResponse.success(Map.of(
                "token", token
        ));
    }
}