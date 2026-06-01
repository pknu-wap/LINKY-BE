package com.project.linkybe_project.service;

import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.dto.LinkUpdateRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.SummaryStatus;
import com.project.linkybe_project.exception.CustomException;
import com.project.linkybe_project.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    // 링크 엔티티 조회와 저장을 담당하는 저장소
    private final LinkRepository linkRepository;

    // 링크 요약 생성을 비동기로 처리하는 서비스
    private final LinkSummaryAsyncService linkSummaryAsyncService;

    @Transactional
    // 새 링크를 저장하고 필요한 경우 요약 생성을 예약함
    public LinkResponse saveLink(String deviceUuid, LinkRequest request) {
        log.info("Link save requested - deviceUuid: {}, url: {}", deviceUuid, request.getUrl());

        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw CustomException.badRequest("URL is required");
        }

        Link link = new Link();
        link.setDeviceUuid(deviceUuid);
        link.setUrl(request.getUrl());
        link.setTitle(request.getTitle());
        link.setCategory(request.getCategory());
        link.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false);
        link.setSelectedDate(request.getSelectedDate());
        link.setIsFavorite(request.getIsFavorite() != null ? request.getIsFavorite() : false);

        List<String> categoryCandidates = sanitizeCategoryCandidates(request.getCategories());
        boolean shouldGenerateSummary = applyCachedSummaryOrMarkPending(link);
        Link savedLink = linkRepository.save(link);

        if (shouldGenerateSummary || !categoryCandidates.isEmpty()) {
            scheduleSummaryGenerationAfterCommit(savedLink.getId(), categoryCandidates);
        }

        return new LinkResponse(savedLink);
    }

    @Transactional
    // 특정 기기의 링크 요약을 직접 수정함
    public LinkResponse updateLinkSummaryByDevice(Long linkId, String deviceUuid, String newSummary) {
        Link link = getLink(linkId, deviceUuid);
        link.markSummaryDone(newSummary);
        log.info("Link summary updated - deviceUuid: {}, linkId: {}", deviceUuid, linkId);
        return new LinkResponse(link);
    }

    @Transactional(readOnly = true)
    // 특정 기기의 모든 링크를 최신순으로 조회함
    public List<LinkResponse> getLinks(String deviceUuid) {
        return linkRepository.findByDeviceUuidOrderByIdDesc(deviceUuid)
                .stream().map(LinkResponse::new).toList();
    }

    @Transactional(readOnly = true)
    // 특정 기기의 링크를 카테고리별로 조회함
    public List<LinkResponse> getLinksByCategory(String deviceUuid, String category) {
        return linkRepository.findByDeviceUuidAndCategoryOrderByIdDesc(deviceUuid, category)
                .stream().map(LinkResponse::new).toList();
    }

    @Transactional(readOnly = true)
    // URL이나 제목에 키워드가 포함된 링크를 검색함
    public List<LinkResponse> searchLinks(String deviceUuid, String keyword) {
        return linkRepository
                .findByDeviceUuidAndUrlContainingOrDeviceUuidAndTitleContainingOrderByIdDesc(
                        deviceUuid, keyword, deviceUuid, keyword)
                .stream().map(LinkResponse::new).toList();
    }

    @Transactional
    // 요청에 포함된 값만 기존 링크에 반영함
    public LinkResponse updateLink(String deviceUuid, Long linkId, LinkUpdateRequest request) {
        Link link = getLink(linkId, deviceUuid);

        if (request.getTitle() != null) link.setTitle(request.getTitle());
        if (request.getCategory() != null) link.setCategory(request.getCategory());
        if (request.getIsPrivate() != null) link.setIsPrivate(request.getIsPrivate());
        if (request.getSelectedDate() != null) link.setSelectedDate(request.getSelectedDate());
        if (request.getIsFavorite() != null) link.setIsFavorite(request.getIsFavorite());

        return new LinkResponse(link);
    }

    @Transactional
    // 특정 기기의 링크 하나를 삭제함
    public void deleteLink(String deviceUuid, Long linkId) {
        Link link = getLink(linkId, deviceUuid);

        linkRepository.delete(link);
        log.info("Link deleted - deviceUuid: {}, linkId: {}", deviceUuid, linkId);
    }

    @Transactional
    // 특정 기기에 저장된 모든 링크를 삭제함
    public long resetLinks(String deviceUuid) {
        long deletedCount = linkRepository.deleteByDeviceUuid(deviceUuid);
        log.info("Links reset - deviceUuid: {}, deletedCount: {}", deviceUuid, deletedCount);
        return deletedCount;
    }

    @Transactional(readOnly = true)
    // 특정 기기의 링크 목록을 CSV 파일 내용으로 변환함
    public byte[] exportCsv(String deviceUuid) {
        List<Link> links = linkRepository.findByDeviceUuidOrderByIdDesc(deviceUuid);

        StringBuilder sb = new StringBuilder();
        sb.append("id,url,title,category,isPrivate,selectedDate,isFavorite,summary,deviceUuid\n");

        for (Link link : links) {
            sb.append(link.getId()).append(",");
            sb.append(escapeCsv(link.getUrl())).append(",");
            sb.append(escapeCsv(link.getTitle())).append(",");
            sb.append(escapeCsv(link.getCategory())).append(",");
            sb.append(link.getIsPrivate() != null ? link.getIsPrivate() : false).append(",");
            sb.append(link.getSelectedDate() != null ? link.getSelectedDate() : "").append(",");
            sb.append(link.getIsFavorite() != null ? link.getIsFavorite() : false).append(",");
            sb.append(escapeCsv(link.getSummary())).append(",");
            sb.append(escapeCsv(link.getDeviceUuid())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    // CSV 내용을 읽어 특정 기기의 링크 목록으로 저장함
    public int importCsv(String deviceUuid, String csvContent) {
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
            link.setDeviceUuid(deviceUuid);
            link.setUrl(url);
            link.setTitle(parts.length > 2 ? unescapeCsv(parts[2]) : null);
            link.setCategory(parts.length > 3 ? unescapeCsv(parts[3]) : null);
            link.setIsPrivate(parts.length > 4 && "true".equalsIgnoreCase(parts[4].trim()));
            link.setIsFavorite(parts.length > 6 && "true".equalsIgnoreCase(parts[6].trim()));

            String summary = parts.length > 7 ? unescapeCsv(parts[7]) : null;
            if (summary == null || summary.isBlank()) {
                link.markSummaryPending();
            } else {
                link.markSummaryDone(summary);
            }

            linkRepository.save(link);
            count++;
        }

        log.info("CSV import completed - deviceUuid: {}, count: {}", deviceUuid, count);
        return count;
    }

    // 링크 ID와 기기 UUID가 모두 일치하는 링크만 가져온다.
    private Link getLink(Long linkId, String deviceUuid) {
        return linkRepository.findByIdAndDeviceUuid(linkId, deviceUuid)
                .orElseThrow(() -> CustomException.notFound("Link not found or access denied"));
    }

    // 같은 URL의 완료된 요약이 있으면 재사용하고 없으면 대기 상태로 표시
    private boolean applyCachedSummaryOrMarkPending(Link link) {
        return linkRepository
                .findFirstByUrlAndSummaryStatusAndSummaryIsNotNullOrderByIdDesc(link.getUrl(), SummaryStatus.DONE)
                .map(cachedLink -> {
                    if (LinkTitlePolicy.shouldReplaceTitle(link.getTitle())
                            && cachedLink.getTitle() != null && !cachedLink.getTitle().isBlank()) {
                        link.setTitle(cachedLink.getTitle());
                    }
                    link.markSummaryDone(cachedLink.getSummary());
                    log.info("Reused cached summary - url: {}, summaryLength: {}",
                            link.getUrl(), cachedLink.getSummary().length());
                    return false;
                })
                .orElseGet(() -> {
                    link.markSummaryPending();
                    return true;
                });
    }

    // 트랜잭션 커밋 이후 비동기 요약 생성을 실행함
    private void scheduleSummaryGenerationAfterCommit(Long linkId, List<String> categoryCandidates) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            linkSummaryAsyncService.generateSummary(linkId, categoryCandidates);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                linkSummaryAsyncService.generateSummary(linkId, categoryCandidates);
            }
        });
    }

    // 자동 분류에 사용할 카테고리 후보에서 빈 값과 기본 탭 이름을 제거
    private List<String> sanitizeCategoryCandidates(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        Set<String> uniqueCategories = new LinkedHashSet<>();
        for (String category : categories) {
            if (category == null || category.isBlank()) {
                continue;
            }

            String trimmedCategory = category.trim();
            if ("전체".equals(trimmedCategory) || "즐겨찾기".equals(trimmedCategory)) {
                continue;
            }

            uniqueCategories.add(trimmedCategory);
        }

        return new ArrayList<>(uniqueCategories);
    }

    // CSV 필드에 필요한 따옴표 이스케이프를 적용
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // CSV 필드의 따옴표 이스케이프를 원래 문자열로 되돌린다.
    private String unescapeCsv(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value.isBlank() ? null : value;
    }
}
