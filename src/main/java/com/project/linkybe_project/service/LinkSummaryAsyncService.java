package com.project.linkybe_project.service;

import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.SummaryStatus;
import com.project.linkybe_project.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkSummaryAsyncService {

    // 자동 카테고리 적용에 필요한 최소 신뢰도이다.
    private static final int CATEGORY_CONFIDENCE_THRESHOLD = 50;

    // 링크 조회와 저장에 사용하는 저장소이다.
    private final LinkRepository linkRepository;

    // 링크 요약과 카테고리 분류를 요청하는 Gemini 서비스이다.
    private final GeminiService geminiService;

    @Async
    @Transactional
    // 카테고리 후보 없이 링크 요약을 비동기로 생성한다.
    public void generateSummary(Long linkId) {
        generateSummary(linkId, List.of());
    }

    @Async
    @Transactional
    // 링크 요약을 생성하고 필요하면 카테고리를 자동 적용한다.
    public void generateSummary(Long linkId, List<String> categoryCandidates) {
        Link link = linkRepository.findById(linkId).orElse(null);
        if (link == null) {
            log.warn("Summary generation skipped because link was not found - linkId: {}", linkId);
            return;
        }

        if (link.getSummaryStatus() == SummaryStatus.DONE) {
            applyAutoCategory(link, link.getSummary(), categoryCandidates);
            return;
        }

        try {
            link.markSummaryProcessing();
            log.info("Async Gemini summary generation started - linkId: {}, url: {}", link.getId(), link.getUrl());

            GeminiSummaryResult result = geminiService.summarizeUrlWithTitle(link.getUrl());
            String summary = result.summary();
            if (!result.successful() || summary == null || summary.isBlank()) {
                markSummaryFailed(link);
            } else {
                if (LinkTitlePolicy.shouldReplaceTitle(link.getTitle())
                        && result.title() != null && !result.title().isBlank()) {
                    link.setTitle(result.title());
                }
                link.markSummaryDone(summary);
                applyAutoCategory(link, summary, categoryCandidates);
            }

            log.info("Async Gemini summary generation completed - linkId: {}, status: {}, title: {}, summaryLength: {}",
                    link.getId(), link.getSummaryStatus(), link.getTitle(),
                    link.getSummary() != null ? link.getSummary().length() : 0);
        } catch (Exception e) {
            markSummaryFailed(link);
            log.error("Async Gemini summary generation failed - linkId: {}, error: {}", link.getId(), e.getMessage());
        }
    }

    // 요약 실패 메시지와 제목 실패 메시지를 링크에 저장한다.
    private void markSummaryFailed(Link link) {
        if (LinkTitlePolicy.shouldReplaceTitle(link.getTitle())) {
            link.setTitle(LinkSummaryMessages.TITLE_FAILED_MESSAGE);
        }
        link.markSummaryFailed(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE);
    }

    // 카테고리가 비어 있으면 Gemini 분류 결과를 기준으로 자동 적용한다.
    private void applyAutoCategory(Link link, String summary, List<String> categoryCandidates) {
        if (!shouldReplaceCategory(link.getCategory()) || categoryCandidates == null || categoryCandidates.isEmpty()) {
            return;
        }

        GeminiClassificationResult classification = geminiService.classifyCategory(summary, categoryCandidates);
        if (!classification.successful() || classification.confidence() < CATEGORY_CONFIDENCE_THRESHOLD) {
            log.info("Auto category skipped - linkId: {}, confidence: {}",
                    link.getId(), classification.confidence());
            return;
        }

        link.setCategory(classification.category());
        log.info("Auto category applied - linkId: {}, category: {}, confidence: {}",
                link.getId(), classification.category(), classification.confidence());
    }

    // 기본 카테고리이거나 비어 있는 경우 자동 교체를 허용한다.
    private boolean shouldReplaceCategory(String category) {
        return category == null || category.isBlank() || "전체".equals(category.trim());
    }
}
