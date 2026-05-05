package com.project.linkybe_project.service;

import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.dto.LinkUpdateRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.exception.CustomException;
import com.project.linkybe_project.repository.LinkRepository;
import com.project.linkybe_project.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public LinkResponse saveLink(String kakaoId, LinkRequest request) {
        log.info(
            "링크 저장 요청 — kakaoId: {}, url: {}",
            kakaoId,
            request.getUrl()
        );

        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw CustomException.badRequest("URL은 필수입니다.");
        }

        User user = getUser(kakaoId);

        Link link = new Link();
        link.setUrl(request.getUrl());
        link.setTitle(request.getTitle());
        link.setCategory(request.getCategory());
        link.setIsPrivate(
            request.getIsPrivate() != null ? request.getIsPrivate() : false
        );
        link.setSelectedDate(request.getSelectedDate());
        link.setUser(user);
        link.setKakaoId(user.getKakaoId());

        Link saved = linkRepository.save(link);
        log.info("링크 저장 완료 — linkId: {}", saved.getId());
        return new LinkResponse(saved);
    }

    // ───────────────────────────────────────────────
    // 링크 전체 조회 (본인 링크만, 최신순)
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LinkResponse> getLinks(String kakaoId) {
        User user = getUser(kakaoId);
        return linkRepository
            .findByUserOrderByIdDesc(user)
            .stream()
            .map(LinkResponse::new)
            .toList();
    }

    // ───────────────────────────────────────────────
    // 카테고리 필터 조회 (본인 링크만)
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LinkResponse> getLinksByCategory(
        String kakaoId,
        String category
    ) {
        User user = getUser(kakaoId);
        return linkRepository
            .findByUserAndCategoryOrderByIdDesc(user, category)
            .stream()
            .map(LinkResponse::new)
            .toList();
    }

    // ───────────────────────────────────────────────
    // 키워드 검색 (URL / 제목, 본인 링크만)
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LinkResponse> searchLinks(String kakaoId, String keyword) {
        User user = getUser(kakaoId);
        return linkRepository
            .findByUserAndUrlContainingOrUserAndTitleContainingOrderByIdDesc(
                user,
                keyword,
                user,
                keyword
            )
            .stream()
            .map(LinkResponse::new)
            .toList();
    }

    // ───────────────────────────────────────────────
    // 링크 수정 (본인 링크만)
    // ───────────────────────────────────────────────
    @Transactional
    public LinkResponse updateLink(
        String kakaoId,
        Long linkId,
        LinkUpdateRequest request
    ) {
        User user = getUser(kakaoId);

        // user + id 동시 조건 → 타 유저 링크 수정 차단
        Link link = linkRepository
            .findByIdAndUser(linkId, user)
            .orElseThrow(() ->
                CustomException.notFound(
                    "링크를 찾을 수 없거나 수정 권한이 없습니다."
                )
            );

        if (request.getTitle() != null) link.setTitle(request.getTitle());
        if (request.getCategory() != null) link.setCategory(
            request.getCategory()
        );
        if (request.getIsPrivate() != null) link.setIsPrivate(
            request.getIsPrivate()
        );
        if (request.getSelectedDate() != null) link.setSelectedDate(
            request.getSelectedDate()
        );

        // @Transactional 더티 체킹으로 자동 UPDATE
        return new LinkResponse(link);
    }

    // ───────────────────────────────────────────────
    // 링크 삭제 (본인 링크만)
    //
    // [핵심] findByIdAndUser → user + id 동시 검증
    // deleteById(id)만 쓰면 타 유저 링크도 삭제 가능한 보안 버그 발생
    // ───────────────────────────────────────────────
    @Transactional
    public void deleteLink(String kakaoId, Long linkId) {
        User user = getUser(kakaoId);

        Link link = linkRepository
            .findByIdAndUser(linkId, user)
            .orElseThrow(() ->
                CustomException.notFound(
                    "링크를 찾을 수 없거나 삭제 권한이 없습니다."
                )
            );

        linkRepository.delete(link);
        log.info("링크 삭제 완료 — kakaoId: {}, linkId: {}", kakaoId, linkId);
    }

    // ───────────────────────────────────────────────
    // CSV 내보내기
    // ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public byte[] exportCsv(String kakaoId) {
        User user = getUser(kakaoId);
        List<Link> links = linkRepository.findByUserOrderByIdDesc(user);

        StringBuilder sb = new StringBuilder();
        sb.append("id,url,title,category,isPrivate,selectedDate\n");

        for (Link link : links) {
            sb.append(link.getId()).append(",");
            sb.append(escapeCsv(link.getUrl())).append(",");
            sb.append(escapeCsv(link.getTitle())).append(",");
            sb.append(escapeCsv(link.getCategory())).append(",");
            sb
                .append(
                    link.getIsPrivate() != null ? link.getIsPrivate() : false
                )
                .append(",");
            sb
                .append(
                    link.getSelectedDate() != null ? link.getSelectedDate() : ""
                )
                .append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ───────────────────────────────────────────────
    // CSV 불러오기
    // ───────────────────────────────────────────────
    @Transactional
    public int importCsv(String kakaoId, String csvContent) {
        User user = getUser(kakaoId);
        String[] lines = csvContent.split("\n");
        int count = 0;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",", -1);
            if (parts.length < 2) continue;

            String url = unescapeCsv(parts[1]);
            if (url == null || url.isBlank()) continue;

            Link link = new Link();
            link.setUrl(url);
            link.setTitle(parts.length > 2 ? unescapeCsv(parts[2]) : null);
            link.setCategory(parts.length > 3 ? unescapeCsv(parts[3]) : null);
            link.setIsPrivate(
                parts.length > 4 && "true".equalsIgnoreCase(parts[4].trim())
            );
            link.setUser(user);
            link.setKakaoId(user.getKakaoId());

            linkRepository.save(link);
            count++;
        }

        log.info("CSV 불러오기 완료 — kakaoId: {}, count: {}", kakaoId, count);
        return count;
    }

    // ───────────────────────────────────────────────
    // 내부 유틸
    // ───────────────────────────────────────────────
    private User getUser(String kakaoId) {
        return userRepository
            .findByKakaoId(kakaoId)
            .orElseThrow(() ->
                CustomException.notFound("존재하지 않는 유저입니다.")
            );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (
            value.contains(",") || value.contains("\"") || value.contains("\n")
        ) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String unescapeCsv(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value
                .substring(1, value.length() - 1)
                .replace("\"\"", "\"");
        }
        return value.isBlank() ? null : value;
    }
}
