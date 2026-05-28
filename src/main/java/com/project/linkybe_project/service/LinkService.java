package com.project.linkybe_project.service;

import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.dto.LinkUpdateRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.exception.CustomException;
import com.project.linkybe_project.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final GeminiResponse geminiResponse;

    @Transactional
    public LinkResponse saveLink(String deviceUuid, LinkRequest request) {
        log.info("Link save requested - deviceUuid: {}, url: {}", deviceUuid, request.getUrl());

        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw CustomException.badRequest("URL은 필수입니다.");
        }

        Link link = new Link();
        link.setDeviceUuid(deviceUuid);
        link.setUrl(request.getUrl());
        link.setTitle(request.getTitle());
        link.setCategory(request.getCategory());
        link.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false);
        link.setSelectedDate(request.getSelectedDate());

        log.info("Gemini summary generation started");
        String aiSummary = geminiResponse.generateSummary(request.getUrl());
        link.updateSummary(aiSummary);
        log.info("Gemini summary generation completed");

        Link savedLink = linkRepository.save(link);
        return new LinkResponse(savedLink);
    }

    @Transactional
    public LinkResponse updateLinkSummaryByDevice(Long linkId, String deviceUuid, String newSummary) {
        Link link = getLink(linkId, deviceUuid);
        link.updateSummary(newSummary);
        log.info("Link summary updated - deviceUuid: {}, linkId: {}", deviceUuid, linkId);
        return new LinkResponse(link);
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> getLinks(String deviceUuid) {
        return linkRepository.findByDeviceUuidOrderByIdDesc(deviceUuid)
                .stream().map(LinkResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> getLinksByCategory(String deviceUuid, String category) {
        return linkRepository.findByDeviceUuidAndCategoryOrderByIdDesc(deviceUuid, category)
                .stream().map(LinkResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> searchLinks(String deviceUuid, String keyword) {
        return linkRepository
                .findByDeviceUuidAndUrlContainingOrDeviceUuidAndTitleContainingOrderByIdDesc(
                        deviceUuid, keyword, deviceUuid, keyword)
                .stream().map(LinkResponse::new).toList();
    }

    @Transactional
    public LinkResponse updateLink(String deviceUuid, Long linkId, LinkUpdateRequest request) {
        Link link = getLink(linkId, deviceUuid);

        if (request.getTitle() != null) link.setTitle(request.getTitle());
        if (request.getCategory() != null) link.setCategory(request.getCategory());
        if (request.getIsPrivate() != null) link.setIsPrivate(request.getIsPrivate());
        if (request.getSelectedDate() != null) link.setSelectedDate(request.getSelectedDate());

        return new LinkResponse(link);
    }

    @Transactional
    public void deleteLink(String deviceUuid, Long linkId) {
        Link link = getLink(linkId, deviceUuid);

        linkRepository.delete(link);
        log.info("Link deleted - deviceUuid: {}, linkId: {}", deviceUuid, linkId);
    }

    @Transactional(readOnly = true)
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
            link.setSummary(parts.length > 7 ? unescapeCsv(parts[7]) : null);

            linkRepository.save(link);
            count++;
        }

        log.info("CSV import completed - deviceUuid: {}, count: {}", deviceUuid, count);
        return count;
    }

    private Link getLink(Long linkId, String deviceUuid) {
        return linkRepository.findByIdAndDeviceUuid(linkId, deviceUuid)
                .orElseThrow(() -> CustomException.notFound("링크를 찾을 수 없거나 권한이 없습니다."));
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
