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
        //프론터에서 받아온 기본 정보 세팅
        //크롤링해온 메타데이터 정보 세팅
        //프론트엔드에서 넘어온 추가 정보 세팅
        link.setUrl(request.getUrl());
        link.setTitle(request.getTitle());
        link.setCategory(request.getCategory());
        link.setIsPrivate(request.getIsPrivate());
        link.setSelectedDate(request.getSelectedDate());

        link.setUser(user);

        //DB 저장이요 하하하
        linkRepository.save(link);
        return null;
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
