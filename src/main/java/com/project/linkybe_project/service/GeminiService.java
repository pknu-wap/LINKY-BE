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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private static final int MAX_CONTENT_LENGTH = 12000;
    private static final int CONNECT_TIMEOUT_MILLIS = (int) Duration.ofSeconds(5).toMillis();
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; LinkyBot/1.0)";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String apiKey;

    public String summarizeUrl(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Skipping summary generation.");
            return "";
        }

        try {
            String normalizedUrl = normalizeUrl(url);
            String pageText = fetchPageText(normalizedUrl);
            if (pageText.isBlank()) {
                log.warn("Page content is empty - url: {}", normalizedUrl);
                return "Page content could not be loaded for summarization.";
            }

            return generateSummary(pageText);
        } catch (Exception e) {
            log.error("Gemini summary failed - url: {}, error: {}", url, e.getMessage());
            return "An error occurred while generating the summary.";
        }
    }

    public String generateSummary(String targetContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Skipping summary generation.");
            return "";
        }

        try {
            String prompt = """
                    Summarize the following web page content in Korean in 3 to 5 sentences.
                    Do not use markdown. Return plain text only.

                    %s
                    """.formatted(limitLength(targetContent));

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_API_URL + apiKey,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return extractTextFromResponse(response.getBody());
        } catch (Exception e) {
            log.error("Gemini API call failed - error: {}", e.getMessage());
            return "An error occurred while generating the summary.";
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

    private String extractTextFromResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            return "Summary result could not be loaded.";
        }

        return textNode.asText();
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
