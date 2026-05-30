package com.project.linkybe_project.service;

import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.SummaryStatus;
import com.project.linkybe_project.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkSummaryAsyncService {

    private static final String SUMMARY_FAILED_MESSAGE = "링크 주소를 요약할 수 없습니다";
    private static final String TITLE_FAILED_MESSAGE = "제목을 생성할 수 없습니다";

    private final LinkRepository linkRepository;
    private final GeminiService geminiService;

    @Async
    @Transactional
    public void generateSummary(Long linkId) {
        Link link = linkRepository.findById(linkId).orElse(null);
        if (link == null) {
            log.warn("Summary generation skipped because link was not found - linkId: {}", linkId);
            return;
        }

        if (link.getSummaryStatus() == SummaryStatus.DONE) {
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
                if (shouldReplaceTitle(link.getTitle()) && result.title() != null && !result.title().isBlank()) {
                    link.setTitle(result.title());
                }
                link.markSummaryDone(summary);
            }

            log.info("Async Gemini summary generation completed - linkId: {}, status: {}, title: {}, summaryLength: {}",
                    link.getId(), link.getSummaryStatus(), link.getTitle(),
                    link.getSummary() != null ? link.getSummary().length() : 0);
        } catch (Exception e) {
            markSummaryFailed(link);
            log.error("Async Gemini summary generation failed - linkId: {}, error: {}", link.getId(), e.getMessage());
        }
    }

    private void markSummaryFailed(Link link) {
        if (shouldReplaceTitle(link.getTitle())) {
            link.setTitle(TITLE_FAILED_MESSAGE);
        }
        link.markSummaryFailed(SUMMARY_FAILED_MESSAGE);
    }

    private boolean shouldReplaceTitle(String title) {
        return title == null || title.isBlank() || "요약중입니다...".equals(title.trim());
    }
}
