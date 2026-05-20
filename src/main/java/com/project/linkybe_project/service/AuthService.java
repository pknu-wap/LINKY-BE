package com.project.linkybe_project.service;

import com.project.linkybe_project.config.JwtUtil;
import com.project.linkybe_project.dto.TokenResponse;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.exception.CustomException;
import com.project.linkybe_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenResponse kakaoLogin(Map<String, String> userInfo) {
        String kakaoId = userInfo.get("kakaoId");

        // 신규 유저면 DB 저장, 기존 유저면 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKakaoId(kakaoId);
                    User saved = userRepository.save(newUser);
                    log.info("신규 유저 등록 — kakaoId: {}", kakaoId);
                    return saved;
                });

        String accessToken  = jwtUtil.generateToken(user.getKakaoId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getKakaoId());

        // refreshToken DB 저장 (탈취 방지용 서버 보관)
        user.setRefreshToken(refreshToken);
        // @Transactional 더티 체킹으로 자동 UPDATE — save() 생략 가능하지만 명시
        userRepository.save(user);

        log.info("로그인 완료 — kakaoId: {}", kakaoId);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        // 1. 서명/만료 검증
        if (!jwtUtil.validate(refreshToken)) {
            throw CustomException.unauthorized("유효하지 않거나 만료된 리프레시 토큰입니다.");
        }

        // 2. 토큰에서 kakaoId 추출
        String kakaoId = jwtUtil.getKakaoId(refreshToken);

        // 3. DB 유저 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> CustomException.notFound("존재하지 않는 유저입니다."));

        // 4. DB 저장 토큰과 비교 (탈취된 토큰 차단)
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw CustomException.unauthorized("리프레시 토큰이 일치하지 않습니다. 재로그인이 필요합니다.");
        }

        // 5. 새 토큰 발급 (Rotation 방식 — 리프레시 토큰도 교체)
        String newAccessToken  = jwtUtil.generateToken(kakaoId);
        String newRefreshToken = jwtUtil.generateRefreshToken(kakaoId);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        log.info("토큰 갱신 완료 — kakaoId: {}", kakaoId);
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