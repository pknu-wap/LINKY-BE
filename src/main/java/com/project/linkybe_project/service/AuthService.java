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

    public void signup(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password); // 👉 나중에 암호화
        userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("비밀번호 틀림");
        }

        return jwtUtil.generateToken(email);
    }
}