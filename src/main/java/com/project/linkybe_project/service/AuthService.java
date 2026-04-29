package com.project.linkybe_project.service;

import com.project.linkybe_project.config.JwtUtil;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public String kakaoLogin(Map<String, String> userInfo) {
        String kakaoId = userInfo.get("kakaoId");
        String email = userInfo.get("email");

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKakaoId(kakaoId);
                    newUser.setEmail(email.isEmpty() ? null : email); // 이메일 없으면 null
                    newUser.setProvider("KAKAO");
                    return userRepository.save(newUser);
                });

        // JWT 주체(subject)를 kakaoId 기준으로 발급
        return jwtUtil.generateToken(user.getKakaoId());
    }
}
