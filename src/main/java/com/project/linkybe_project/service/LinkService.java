package com.project.linkybe_project.service;

import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.LinkRepository;
import com.project.linkybe_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final GeminiResponse geminiResponse;

    // ───────────────────────────────────────────────
    // 링크 저장
    // ───────────────────────────────────────────────
    @Transactional
    public LinkResponse saveLink_DB(String kakaoId, LinkRequest request) {
        log.info("=== saveLink_DB 호출됨 ===");
        log.info("kakaoId : {}", kakaoId);
        log.info("url : {}", request.getUrl());

        User user = getUser(kakaoId);
        log.info("유저발견 : {}", user.getId());

        Link link = new Link();
        //프론트에서 받아온 기본 정보 세팅
        //크롤링해온 메타데이터 정보 세팅
        //프론트엔드에서 넘어온 추가 정보 세팅
        link.setUrl(request.getUrl());
        link.setTitle(request.getTitle());
        link.setCategory(request.getCategory());
        link.setIsPrivate(request.getIsPrivate());
        link.setSelectedDate(request.getSelectedDate());

        link.setUser(user);

        // Gemini를 이용한 요약 생성 및 엔티티에 세팅
        log.info("Gemini 요약 생성 시작...");
        String aiSummary = geminiResponse.generateSummary(request.getUrl()); // 임시로 URL 전달
        link.updateSummary(aiSummary);
        log.info("Gemini 요약 완료: {}", aiSummary);

        //DB 저장 후 반환 객체 변수에 담기 (수정된 부분)
        Link savedLink = linkRepository.save(link);

        // 저장된 데이터를 DTO로 변환하여 반환
        return new LinkResponse(savedLink);
    }

    // ───────────────────────────────────────────────
    // 사용자 직접 요약 수정 (새로 추가된 기능!)
    // ───────────────────────────────────────────────
    @Transactional
    public LinkResponse updateLinkSummaryByUser(Long linkId, String kakaoId, String newSummary) {
        log.info("=== updateLinkSummaryByUser 호출됨 ===");

        // 1. 유저 검증
        User user = getUser(kakaoId);

        // 2. 수정할 링크 찾기
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new RuntimeException("해당 링크를 찾을 수 없습니다."));

        // 3. 본인의 링크인지 권한 체크 (보안상 필수)
        if (!link.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 4. 엔티티의 메서드를 통해 새로운 요약본으로 덮어쓰기
        link.updateSummary(newSummary);
        log.info("링크 요약 수정 완료 - linkId: {}", linkId);

        // 5. 업데이트된 결과를 DTO로 변환하여 프론트엔드로 반환
        return new LinkResponse(link);
    }

    // ───────────────────────────────────────────────
    // 링크 전체 조회 (최신순)
    // ───────────────────────────────────────────────
    @Transactional
    public List<LinkResponse> getLinks(String kakaoId) {
        User user = getUser(kakaoId);
        return linkRepository.findByUserOrderByIdDesc(user)
                .stream().map(LinkResponse::new).toList();
    }

    // ───────────────────────────────────────────────
    // 내부 유틸
    // ───────────────────────────────────────────────
    private User getUser(String kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
    }
}