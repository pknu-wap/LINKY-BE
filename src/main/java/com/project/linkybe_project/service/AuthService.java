package com.project.linkybe_project.service;

import com.project.linkybe_project.config.JwtUtil;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public String kakaoLogin(String email) {

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setPassword("KAKAO"); // 의미 없음
                    return userRepository.save(newUser);
                });

        return jwtUtil.generateToken(user.getEmail());
    }
}