package com.project.linkybe_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    // Gemini 2.5 Flash 모델
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    // application.yaml의 gemini.api-key 값을 자동으로 주입받음
    @Value("${gemini.api-key}")
    private String apiKey;

    public String summarizeUrl(String url) {
        try {
            // Gemini에게 보낼 프롬프트 구성
            String prompt = "다음 URL의 웹페이지 내용을 한국어로 3~5문장으로 요약해줘. URL: " + url;

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Gemini API에 POST 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_API_URL + apiKey,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 응답 JSON에서 실제 텍스트로 반환
            JsonNode root = objectMapper.readTree(response.getBody());
            return root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText("요약 결과를 가져올 수 없습니다.");

        } catch (Exception e) {
            log.error("Gemini API 호출 실패 — url: {}, error: {}", url, e.getMessage());
            return "요약 중 오류가 발생했습니다.";
        }
    }
}