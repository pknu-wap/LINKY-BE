package com.project.linkybe_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiResponse {

    // 외부 API 호출을 위한 RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();
    // application.yml에 등록한 API 키를 가져옵니다.
    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateSummary(String targetContent) {
        // 1. API 요청 URL 세팅 (최신 모델인 gemini-1.5-flash 기준)
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // 2. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. 시스템 프롬프트 및 바디(Body) 데이터 구성
        String prompt = "다음 내용을 3줄 이내로 핵심만 요약해줘. 마크다운 기호 없이 평문으로만 작성해:\n\n" + targetContent;

        // Gemini API가 요구하는 중첩된 JSON 구조를 Map으로 만듭니다.
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(parts));

        // 4. HTTP 요청 엔티티 생성
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 5. API 호출 (POST 방식) 및 응답 받기
            // JsonNode 형태로 받아오면 복잡한 DTO 클래스를 만들지 않고도 원하는 데이터만 추출하기 편합니다.
            JsonNode response = restTemplate.postForObject(url, requestEntity, JsonNode.class);

            // 6. 응답 JSON에서 요약된 텍스트만 파싱해서 반환
            return extractTextFromResponse(response);

        } catch (Exception e) {
            // API 호출 실패 시 에러 처리
            e.printStackTrace();
            return "요약을 생성하는 중 오류가 발생했습니다.";
        }
    }

    // JSON 응답 구조를 뚫고 들어가 실제 텍스트만 가져오는 헬퍼 메서드
    private String extractTextFromResponse(JsonNode response) {
        if (response != null && response.has("candidates")) {
            JsonNode candidates = response.get("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content.has("parts")) {
                    JsonNode parts = content.get("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }
        }
        return "요약 결과를 파싱할 수 없습니다.";
    }
}
