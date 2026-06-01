package com.project.linkybe_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    // Gemini generateContent API 호출 URL 형식
    private static final String GEMINI_API_URL_FORMAT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    // 요약 생성에 사용할 Gemini 모델 이름
    private static final String SUMMARY_MODEL = "gemini-2.5-flash";

    // Gemini 요청에 포함할 본문 텍스트의 최대 길이
    private static final int MAX_CONTENT_LENGTH = 12000;

    // 웹페이지 HTML을 가져올 때 사용할 연결 제한 시간
    private static final int CONNECT_TIMEOUT_MILLIS = (int) Duration.ofSeconds(5).toMillis();

    // Gemini 503 오류 발생 시 최대 재시도 횟수
    private static final int MAX_GEMINI_ATTEMPTS = 3;

    // 웹페이지 수집 요청에 사용할 User-Agent 값
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; LinkyBot/1.0)";
    // Gemini API 호출에 사용할 HTTP 클라이언트
    private final RestTemplate restTemplate = new RestTemplate();

    // Gemini 응답 JSON을 파싱하는 객체
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Gemini API 인증에 사용할 키
    @Value("${gemini.api.key:}")
    private String apiKey;

    // 카테고리 분류 요청에 사용할 Gemini 모델 이름
    @Value("${gemini.api.classification-model:gemini-2.5-flash-lite}")
    private String classificationModel;

    // URL 요약 결과에서 요약 문자열만 반환
    public String summarizeUrl(String url) {
        return summarizeUrlWithTitle(url).summary();
    }

    // URL의 본문을 가져와 제목과 요약을 함께 생성
    public GeminiSummaryResult summarizeUrlWithTitle(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Skipping summary generation.");
            return GeminiSummaryResult.failure(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE);
        }

        try {
            String normalizedUrl = normalizeUrl(url);
            String pageText = fetchPageText(normalizedUrl);
            if (pageText.isBlank()) {
                log.warn("Page content is empty - url: {}", normalizedUrl);
                return GeminiSummaryResult.failure(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE);
            }

            return generateTitleAndSummary(pageText);
        } catch (Exception e) {
            log.error("Gemini summary failed - url: {}, error: {}", url, e.getMessage());
            return GeminiSummaryResult.failure(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE);
        }
    }

    // 텍스트만 입력받아 요약 문자열만 반환
    public String generateSummary(String targetContent) {
        return generateTitleAndSummary(targetContent).summary();
    }

    // 입력된 텍스트를 Gemini에 보내 제목과 요약 JSON을 생성
    public GeminiSummaryResult generateTitleAndSummary(String targetContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Skipping summary generation.");
            return GeminiSummaryResult.failure(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE);
        }

        try {
            String prompt = """
                    Summarize the web page content into JSON.
                    Format: {"title": "Korean, max 40 chars", "summary": "3-5 sentences plain Korean"}
                    Rules: No markdown in summary. No conversational text. Focus on core utility.

                    %s
                    """.formatted(limitLength(targetContent));

            HttpEntity<Map<String, Object>> entity = createGeminiRequest(prompt);
            ResponseEntity<String> response = requestGemini(entity, SUMMARY_MODEL);

            return extractTitleAndSummaryFromResponse(response.getBody());
        } catch (Exception e) {
            log.error("Gemini API call failed - error: {}", e.getMessage());
            return GeminiSummaryResult.failure(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE);
        }
    }

    // 생성된 요약을 후보 카테고리 중 하나로 분류
    public GeminiClassificationResult classifyCategory(String summary, List<String> categoryCandidates) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Skipping category classification.");
            return GeminiClassificationResult.failure();
        }
        if (summary == null || summary.isBlank() || categoryCandidates == null || categoryCandidates.isEmpty()) {
            return GeminiClassificationResult.failure();
        }

        try {
            String categoryJson = objectMapper.writeValueAsString(categoryCandidates);
            String prompt = """
                    Classify the summary into the closest category.
                    Summary:
                    %s

                    Category candidates:
                    %s

                    Return JSON only.
                    Format: {"category": "one candidate exactly", "confidence": 0-100}
                    Rules:
                    - category must be one of the candidates exactly.
                    - confidence must be an integer from 0 to 100.
                    - If no category clearly matches, choose the closest candidate but use a low confidence.
                    """.formatted(limitLength(summary), categoryJson);

            HttpEntity<Map<String, Object>> entity = createGeminiRequest(prompt);
            ResponseEntity<String> response = requestGemini(entity, classificationModel);

            return extractCategoryClassificationFromResponse(response.getBody(), categoryCandidates);
        } catch (Exception e) {
            log.error("Gemini category classification failed - error: {}", e.getMessage());
            return GeminiClassificationResult.failure();
        }
    }

    // URL의 HTML을 읽고 요약에 필요한 텍스트만 추출
    private String fetchPageText(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(CONNECT_TIMEOUT_MILLIS)
                .followRedirects(true)
                .get();

        document.select("script, style, noscript, iframe, svg").remove();

        String title = document.title();
        String description = getMetaDescription(document);
        String body = document.body() != null ? document.body().text() : "";

        return normalizeText(String.join("\n", title, description, body));
    }

    // Gemini generateContent API가 요구하는 요청 본문을 만듬.
    private HttpEntity<Map<String, Object>> createGeminiRequest(String prompt) {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(requestBody, headers);
    }

    // Gemini API를 호출하고 일시적인 503 오류는 짧게 재시도
    private ResponseEntity<String> requestGemini(HttpEntity<Map<String, Object>> entity, String model)
            throws InterruptedException {
        for (int attempt = 1; attempt <= MAX_GEMINI_ATTEMPTS; attempt++) {
            try {
                return restTemplate.exchange(
                        GEMINI_API_URL_FORMAT.formatted(model, apiKey),
                        HttpMethod.POST,
                        entity,
                        String.class
                );
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() != 503 || attempt == MAX_GEMINI_ATTEMPTS) {
                    throw e;
                }

                long delayMillis = 1000L * attempt;
                log.warn("Gemini API returned 503. Retrying - attempt: {}, delayMillis: {}", attempt, delayMillis);
                Thread.sleep(delayMillis);
            }
        }

        throw new IllegalStateException("Gemini API request failed.");
    }

    // 스킴이 없는 URL에는 https 스킴을 붙임
    private String normalizeUrl(String url) {
        String trimmedUrl = url == null ? "" : url.trim();
        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            return trimmedUrl;
        }
        return "https://" + trimmedUrl;
    }

    // HTML meta description 값을 가져옴.
    private String getMetaDescription(Document document) {
        Element description = document.selectFirst("meta[name=description]");
        return description != null ? description.attr("content") : "";
    }

    // Gemini 요약 응답에서 제목과 요약 필드를 추출함.
    private GeminiSummaryResult extractTitleAndSummaryFromResponse(String responseBody) throws IOException {
        String text = extractTextFromResponse(responseBody);
        if (text.equals(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE)) {
            return GeminiSummaryResult.failure(text);
        }

        JsonNode generated = objectMapper.readTree(stripCodeFence(text));
        String title = generated.path("title").asText("").trim();
        String summary = generated.path("summary").asText("").trim();

        if (summary.isBlank()) {
            return GeminiSummaryResult.failure(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE);
        }

        return GeminiSummaryResult.success(title, summary);
    }

    // Gemini 분류 응답에서 카테고리와 신뢰도를 추출함
    private GeminiClassificationResult extractCategoryClassificationFromResponse(
            String responseBody, List<String> categoryCandidates) throws IOException {
        String text = extractTextFromResponse(responseBody);
        if (text.equals(LinkSummaryMessages.SUMMARY_FAILED_MESSAGE)) {
            return GeminiClassificationResult.failure();
        }

        JsonNode generated = objectMapper.readTree(stripCodeFence(text));
        String category = generated.path("category").asText("").trim();
        int confidence = generated.path("confidence").asInt(0);

        if (category.isBlank() || !categoryCandidates.contains(category)) {
            return GeminiClassificationResult.failure();
        }

        confidence = Math.max(0, Math.min(100, confidence));
        return GeminiClassificationResult.success(category, confidence);
    }

    // Gemini 응답 JSON에서 생성된 텍스트만 꺼냄
    private String extractTextFromResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            return LinkSummaryMessages.SUMMARY_FAILED_MESSAGE;
        }

        return textNode.asText();
    }

    // Gemini가 JSON을 코드 블록으로 감싼 경우 코드 펜스를 제거함
    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "")
                    .replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    // 너무 긴 입력은 Gemini 요청 제한에 맞춰 자름
    private String limitLength(String content) {
        String normalizedContent = normalizeText(content);
        if (normalizedContent.length() <= MAX_CONTENT_LENGTH) {
            return normalizedContent;
        }
        return normalizedContent.substring(0, MAX_CONTENT_LENGTH);
    }

    // 공백 문자를 하나의 공백으로 정규화함
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}
