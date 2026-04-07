package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.AuthRequest;
import com.project.linkybe_project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<?> signup(@RequestBody AuthRequest request) {
        authService.signup(request.getEmail(), request.getPassword());
        return ApiResponse.success(null);
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());

        return ApiResponse.success(Map.of(
                "token", token
        ));
    }
}