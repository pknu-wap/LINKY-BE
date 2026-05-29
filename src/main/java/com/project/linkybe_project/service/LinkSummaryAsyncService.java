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

            String summary = geminiService.summarizeUrl(link.getUrl());
            if (summary == null || summary.isBlank()) {
                link.markSummaryFailed("Summary could not be generated.");
            } else if (summary.startsWith("An error occurred") || summary.startsWith("Page content could not")) {
                link.markSummaryFailed(summary);
            } else {
                link.markSummaryDone(summary);
            }

            log.info("Async Gemini summary generation completed - linkId: {}, status: {}, summaryLength: {}",
                    link.getId(), link.getSummaryStatus(), link.getSummary() != null ? link.getSummary().length() : 0);
        } catch (Exception e) {
            link.markSummaryFailed("Summary could not be generated.");
            log.error("Async Gemini summary generation failed - linkId: {}, error: {}", link.getId(), e.getMessage());
        }
    }
}
