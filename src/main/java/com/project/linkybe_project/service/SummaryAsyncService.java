package com.project.linkybe_project.service;

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
public class SummaryAsyncService {

    private final CrawlerService crawlerService;
    private final KeywordExtractor keywordExtractor;
    private final GeminiResponse geminiResponse;
    private final LinkRepository linkRepository;

    /**
     * @Async → 이 메서드는 별도 스레드에서 실행된다.
     * 호출한 쪽(LinkService)은 기다리지 않고 즉시 반환된다.
     * <p>
     * 전체 흐름:
     * 1. URL 크롤링
     * 2. 키워드 추출
     * 3. Gemini 요약 생성
     * 4. DB 업데이트
     */
    @Async  // Spring이 별도 스레드 풀에서 실행해줌
    @Transactional
    public void processSummary(Long linkId, String url) {
        log.info("[ASYNC] 요약 처리 시작 — linkId: {}, url: {}", linkId, url);

        try {
            // Step 1. HTML 크롤링 → 텍스트 추출 (최대 3000자)
            String text = crawlerService.crawl(url);

            // 크롤링 결과가 비어있으면 요약 불가
            if (text.isBlank()) {
                log.warn("[ASYNC] 크롤링 결과 없음 — linkId: {}", linkId);
                updateSummaryInDb(linkId, "페이지 내용을 가져올 수 없습니다.");
                return;
            }

            // Step 2. 텍스트에서 키워드 추출 (빈도 기반 상위 6개)
            List<String> keywords = keywordExtractor.extract(text);
            log.info("[ASYNC] 키워드 추출 완료 — linkId: {}, keywords: {}", linkId, keywords);

            // Step 3. Gemini API 호출하여 요약 생성
            String summary = geminiResponse.generateSummary(text, keywords);
            log.info("[ASYNC] 요약 생성 완료 — linkId: {}", linkId);

            // Step 4. DB 업데이트
            updateSummaryInDb(linkId, summary);

        } catch (Exception e) {
            // 어떤 예외가 와도 앱이 죽지 않도록 catch
            log.error("[ASYNC] 요약 처리 중 예외 발생 — linkId: {}, error: {}", linkId, e.getMessage());
            updateSummaryInDb(linkId, "요약 처리 중 오류가 발생했습니다.");
        }
    }

    // DB에서 해당 링크를 찾아 summary 필드만 업데이트
    private void updateSummaryInDb(Long linkId, String summary) {
        linkRepository.findById(linkId).ifPresent(link -> {
            link.updateSummary(summary);
            linkRepository.save(link);
            log.info("[ASYNC] DB 업데이트 완료 — linkId: {}", linkId);
        });
    }
}