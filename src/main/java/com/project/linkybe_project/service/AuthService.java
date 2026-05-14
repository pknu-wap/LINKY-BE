package com.project.linkybe_project.service;

import com.project.linkybe_project.config.JwtUtil;
import com.project.linkybe_project.dto.TokenResponse;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenResponse kakaoLogin(Map<String, String> userInfo) {
        String kakaoId = userInfo.get("kakaoId");

        // 신규 유저면 저장, 기존 유저면 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKakaoId(kakaoId);
                    return userRepository.save(newUser);
                });

        // 액세스 토큰 + 리프레시 토큰 발급
        String accessToken = jwtUtil.generateToken(user.getKakaoId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getKakaoId());

        // 리프레시 토큰 DB 저장 (탈취 방지를 위해 서버에서 보관)
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        // 1. 토큰 서명/만료 검증
        if (!jwtUtil.validate(refreshToken)) {
            throw new RuntimeException("유효하지 않거나 만료된 리프레시 토큰입니다.");
        }

        // 2. 토큰에서 kakaoId 추출
        String kakaoId = jwtUtil.getKakaoId(refreshToken);

        // 3. DB에서 유저 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        // 4. DB에 저장된 리프레시 토큰과 비교 (탈취된 토큰 차단)
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("리프레시 토큰이 일치하지 않습니다. 재로그인이 필요합니다.");
        }

        // 5. 새 토큰 발급 (Rotation: 리프레시 토큰도 새로 발급하여 DB 갱신)
        String newAccessToken = jwtUtil.generateToken(kakaoId);
        String newRefreshToken = jwtUtil.generateRefreshToken(kakaoId);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // ───────────────────────────────────────────────
    // 회원 탈퇴 (논리적 삭제)
    // ───────────────────────────────────────────────
    @Transactional
    public void withdrawUser(String kakaoId) {
        // 1. DB에서 유저 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        // 2. 보안을 위해 DB에 저장된 리프레시 토큰 삭제 (로그아웃 효과)
        user.setRefreshToken(null);

        // 3. 유저 삭제
        // (주의: User 엔티티에 @SQLDelete를 설정해 두었으므로,
        // 실제 DB에서 데이터가 날아가지 않고 UPDATE user SET is_deleted = true 가 실행됩니다!)
        userRepository.delete(user);
    }
}