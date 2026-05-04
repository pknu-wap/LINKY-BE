package com.project.linkybe_project.service;

import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.dto.LinkUpdateRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.LinkRepository;
import com.project.linkybe_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        User user = getUser(kakaoId);

        Link link = new Link();
        //프론터에서 받아온 기본 정보 세팅
        //크롤링해온 메타데이터 정보 세팅
        //프론트엔드에서 넘어온 추가 정보 세팅
        link.setUrl(request.getUrl());
        link.setTitle(request.getTitle());
        link.setCategory(request.getCategory());
        link.setIsPrivate(request.getIsPrivate());
        link.setSelectedDate(request.getSelectedDate());


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
    // 카테고리 필터 조회
    // ───────────────────────────────────────────────
    @Transactional
    public List<LinkResponse> getLinksByCategory(String kakaoId, String category) {
        User user = getUser(kakaoId);
        return linkRepository.findByUserAndCategoryOrderByIdDesc(user, category)
                .stream().map(LinkResponse::new).toList();
    }

    // ───────────────────────────────────────────────
    // 링크 검색 (URL / 제목 키워드)
    // ───────────────────────────────────────────────
    @Transactional
    public List<LinkResponse> searchLinks(String kakaoId, String keyword) {
        User user = getUser(kakaoId);
        return linkRepository
                .findByUserAndUrlContainingOrUserAndTitleContainingOrderByIdDesc(
                        user, keyword, user, keyword)
                .stream().map(LinkResponse::new).toList();
    }

    // ───────────────────────────────────────────────
    // 링크 수정 (메모, 카테고리)
    // ───────────────────────────────────────────────
    @Transactional
    public LinkResponse updateLink(String kakaoId, Long linkId, LinkUpdateRequest request) {
        User user = getUser(kakaoId);
        Link link = linkRepository.findByIdAndUser(linkId, user)
                .orElseThrow(() -> new RuntimeException("링크를 찾을 수 없습니다."));

        return new LinkResponse(link);
    }

    // ───────────────────────────────────────────────
    // 링크 삭제
    // ───────────────────────────────────────────────
    @Transactional
    public void deleteLink(String kakaoId, Long linkId) {
        User user = getUser(kakaoId);
        Link link = linkRepository.findByIdAndUser(linkId, user)
                .orElseThrow(() -> new RuntimeException("링크를 찾을 수 없습니다."));
        linkRepository.delete(link);
    }

    // ───────────────────────────────────────────────
    // 내부 유틸
    // ───────────────────────────────────────────────
    private User getUser(String kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String unescapeCsv(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value.isBlank() ? null : value;
    }
}
