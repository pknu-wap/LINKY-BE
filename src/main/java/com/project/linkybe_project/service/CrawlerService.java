package com.project.linkybe_project.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CrawlerService {

    // 최대 텍스트 길이 (3000자 초과시 앞 3000자만 사용)
    private static final int MAX_TEXT_LENGTH = 3000;

    /**
     * URL에서 HTML을 가져와 순수 텍스트로 변환한다.
     * 실패 시 빈 문자열을 반환한다 (예외를 위로 던지지 않음).
     *
     * @param url 크롤링할 웹페이지 주소
     * @return HTML 태그가 제거된 텍스트 (최대 3000자)
     */
    public String crawl(String url) {
        try {
            // Jsoup으로 URL에 접속해서 HTML 문서 가져오기
            Document doc = Jsoup.connect(url)
                    // 403 에러 방지: 실제 크롬 브라우저처럼 위장
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36")
                    .timeout(8000)  // 8초 안에 응답 없으면 포기
                    .get();

            // HTML 태그 전부 제거 → 순수 텍스트만 남김
            // doc.text()는 <p>, <h1>, <div> 등 모든 태그 내용을 이어붙인 텍스트 반환
            String rawText = doc.text();

            // 3000자 초과 시 앞 3000자만 사용 (요구사항)
            if (rawText.length() > MAX_TEXT_LENGTH) {
                return rawText.substring(0, MAX_TEXT_LENGTH);
            }
            return rawText;

        } catch (Exception e) {
            // 크롤링 실패해도 앱이 멈추지 않도록 에러 로그만 남기고 빈 문자열 반환
            log.warn("크롤링 실패 — url: {}, reason: {}", url, e.getMessage());
            return "";
        }
    }
}