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

    private static final String GEMINI_API_URL_FORMAT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String SUMMARY_MODEL = "gemini-2.5-flash";
    private static final int MAX_CONTENT_LENGTH = 12000;
    private static final int CONNECT_TIMEOUT_MILLIS = (int) Duration.ofSeconds(5).toMillis();
    private static final int MAX_GEMINI_ATTEMPTS = 3;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; LinkyBot/1.0)";
    private static final String SUMMARY_FAILED_MESSAGE = "링크 주소를 요약할 수 없습니다";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.classification-model:gemini-2.5-flash-lite}")
    private String classificationModel;

    public String summarizeUrl(String url) {
        return summarizeUrlWithTitle(url).summary();
    }

    public GeminiSummaryResult summarizeUrlWithTitle(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Skipping summary generation.");
            return GeminiSummaryResult.failure(SUMMARY_FAILED_MESSAGE);
        }

        try {
            String normalizedUrl = normalizeUrl(url);
            String pageText = fetchPageText(normalizedUrl);
            if (pageText.isBlank()) {
                log.warn("Page content is empty - url: {}", normalizedUrl);
                return GeminiSummaryResult.failure(SUMMARY_FAILED_MESSAGE);
            }

            return generateTitleAndSummary(pageText);
        } catch (Exception e) {
            log.error("Gemini summary failed - url: {}, error: {}", url, e.getMessage());
            return GeminiSummaryResult.failure(SUMMARY_FAILED_MESSAGE);
        }
    }

    public String generateSummary(String targetContent) {
        return generateTitleAndSummary(targetContent).summary();
    }

    public GeminiSummaryResult generateTitleAndSummary(String targetContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Skipping summary generation.");
            return GeminiSummaryResult.failure(SUMMARY_FAILED_MESSAGE);
        }

        try {
            String prompt = """
                    Summarize the web page content into JSON.
                    Format: {"title": "Korean, max 40 chars", "summary": "3-5 sentences plain Korean"}
                    Rules: No markdown in summary. No conversational text. Focus on core utility.

                    %s
                    """.formatted(limitLength(targetContent));

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = requestGemini(entity, SUMMARY_MODEL);

            return extractTitleAndSummaryFromResponse(response.getBody());
        } catch (Exception e) {
            log.error("Gemini API call failed - error: {}", e.getMessage());
            return GeminiSummaryResult.failure(SUMMARY_FAILED_MESSAGE);
        }
    }

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

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = requestGemini(entity, classificationModel);

            return extractCategoryClassificationFromResponse(response.getBody(), categoryCandidates);
        } catch (Exception e) {
            log.error("Gemini category classification failed - error: {}", e.getMessage());
            return GeminiClassificationResult.failure();
        }
    }

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

    private String normalizeUrl(String url) {
        String trimmedUrl = url == null ? "" : url.trim();
        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            return trimmedUrl;
        }
        return "https://" + trimmedUrl;
    }

    private String getMetaDescription(Document document) {
        Element description = document.selectFirst("meta[name=description]");
        return description != null ? description.attr("content") : "";
    }

    private GeminiSummaryResult extractTitleAndSummaryFromResponse(String responseBody) throws IOException {
        String text = extractTextFromResponse(responseBody);
        if (text.equals(SUMMARY_FAILED_MESSAGE)) {
            return GeminiSummaryResult.failure(text);
        }

        JsonNode generated = objectMapper.readTree(stripCodeFence(text));
        String title = generated.path("title").asText("").trim();
        String summary = generated.path("summary").asText("").trim();

        if (summary.isBlank()) {
            return GeminiSummaryResult.failure(SUMMARY_FAILED_MESSAGE);
        }

        return GeminiSummaryResult.success(title, summary);
    }

    private GeminiClassificationResult extractCategoryClassificationFromResponse(
            String responseBody, List<String> categoryCandidates) throws IOException {
        String text = extractTextFromResponse(responseBody);
        if (text.equals(SUMMARY_FAILED_MESSAGE)) {
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

    private String extractTextFromResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            return SUMMARY_FAILED_MESSAGE;
        }

        return textNode.asText();
    }

    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "")
                    .replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    private String limitLength(String content) {
        String normalizedContent = normalizeText(content);
        if (normalizedContent.length() <= MAX_CONTENT_LENGTH) {
            return normalizedContent;
        }
        return normalizedContent.substring(0, MAX_CONTENT_LENGTH);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}
