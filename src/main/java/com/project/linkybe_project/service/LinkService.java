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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final MetaDataService metaDataService;

    // ───────────────────────────────────────────────
    // 링크 저장
    // ───────────────────────────────────────────────
    @Transactional
    public LinkResponse saveLink(String kakaoId, LinkRequest request) {
        User user = getUser(kakaoId);

        Link link = new Link();
        //프론터에서 받아온 기본 정보 세팅
        link.setUrl(request.getUrl());
        link.assignUser(user);
        //크롤링해온 메타데이터 정보 세팅
        link.setTitle(metadata.title());
        //프론트엔드에서 넘어온 추가 정보 세팅
        link.setCategory(request.getCategory());
        link.setIsPrivate(request.getIsPrivate());
        link.setSelectedDate(request.getSelectedDate());

        //제목 세팅
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            link.setTitle(request.getTitle());
        } else {
            link.setTitle(metadata.title());
        }

        //DB 저장이요 하하하
        linkRepository.save(link);
    }
        link.setMemo(request.getMemo());
        link.setCategory(request.getCategory());
        link.setUser(user);

        return new LinkResponse(linkRepository.save(link));
    }

    // ───────────────────────────────────────────────
    // 링크 전체 조회 (최신순)
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LinkResponse> getLinks(String kakaoId) {
        User user = getUser(kakaoId);
        return linkRepository.findByUserOrderByIdDesc(user)
                .stream().map(LinkResponse::new).toList();
    }

    // ───────────────────────────────────────────────
    // 카테고리 필터 조회
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LinkResponse> getLinksByCategory(String kakaoId, String category) {
        User user = getUser(kakaoId);
        return linkRepository.findByUserAndCategoryOrderByIdDesc(user, category)
                .stream().map(LinkResponse::new).toList();
    }

    // ───────────────────────────────────────────────
    // 링크 검색 (URL / 제목 키워드)
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
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

        if (request.getMemo() != null) link.setMemo(request.getMemo());
        if (request.getCategory() != null) link.setCategory(request.getCategory());

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
    // CSV 내보내기 (문자열 반환, 컨트롤러에서 파일 응답)
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public String exportCsv(String kakaoId) {
        User user = getUser(kakaoId);
        List<Link> links = linkRepository.findByUserOrderByIdDesc(user);

        StringBuilder sb = new StringBuilder();
        sb.append("id,url,title,siteName,category,memo,createdAt\n");

        for (Link link : links) {
            sb.append(link.getId()).append(",");
            sb.append(escapeCsv(link.getUrl())).append(",");
            sb.append(escapeCsv(link.getTitle())).append(",");
            sb.append(escapeCsv(link.getSiteName())).append(",");
            sb.append(escapeCsv(link.getCategory())).append(",");
            sb.append(escapeCsv(link.getMemo())).append(",");
            sb.append(link.getCreatedAt() != null ? link.getCreatedAt() : "").append("\n");
        }

        return sb.toString();
    }

    // ───────────────────────────────────────────────
    // CSV 불러오기 (CSV 문자열 → DB 저장)
    // ───────────────────────────────────────────────
    @Transactional
    public int importCsv(String kakaoId, String csvContent) {
        User user = getUser(kakaoId);
        String[] lines = csvContent.split("\n");
        int count = 0;

        // 첫 번째 줄(헤더) 건너뜀
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            // CSV 파싱: id,url,title,siteName,category,memo,createdAt
            String[] parts = line.split(",", -1);
            if (parts.length < 2) continue;

            String url = unescapeCsv(parts[1]);
            if (url == null || url.isBlank()) continue;

            Link link = new Link();
            link.setUrl(url);
            link.setTitle(parts.length > 2 ? unescapeCsv(parts[2]) : null);
            link.setSiteName(parts.length > 3 ? unescapeCsv(parts[3]) : null);
            link.setCategory(parts.length > 4 ? unescapeCsv(parts[4]) : null);
            link.setMemo(parts.length > 5 ? unescapeCsv(parts[5]) : null);
            link.setUser(user);

            linkRepository.save(link);
            count++;
        }

        return count;
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
