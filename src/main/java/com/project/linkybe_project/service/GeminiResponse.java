package com.project.linkybe_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiResponse {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api-key}")
    private String apiKey;

    /**
     * 본문 텍스트와 키워드를 받아 Gemini API로 요약을 생성한다.
     *
     * @param content  크롤링해서 추출한 본문 텍스트 (최대 3000자)
     * @param keywords 키워드 추출기가 뽑은 키워드 리스트
     * @return Gemini가 생성한 3문장 요약
     */
    public String generateSummary(String content, List<String> keywords) {

        // Gemini API 엔드포인트 URL
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                "gemini-1.5-flash:generateContent?key=" + apiKey;

        // 프롬프트 구성
        String keywordsStr = String.join(", ", keywords); // ["AI", "학습"] → "AI, 학습"
        String prompt = """
                당신은 링크 요약 시스템이다.
                다음 규칙을 반드시 지켜라:
                1. 출력은 정확히 3문장으로 작성한다
                2. 각 문장은 30자 이상 80자 이하로 작성한다
                3. 감정 표현, 과장 표현을 사용하지 않는다
                4. 동일한 의미는 항상 동일한 표현을 사용한다
                5. 첫 문장은 전체 주제를 설명한다
                6. 두 번째 문장은 핵심 내용을 설명한다
                7. 세 번째 문장은 결론 또는 특징을 설명한다
                
                입력:
                본문:
                %s
                
                키워드:
                %s
                
                출력:
                문장1.
                문장2.
                문장3.
                """.formatted(content, keywordsStr);

        // ── HTTP 헤더 설정 ────────────────────────────────────────
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ── 요청 바디(Body) 구성 ──────────────────────────────────
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content_map = new HashMap<>();
        content_map.put("parts", List.of(textPart));

        // temperature, top_p, top_k 설정
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);   // 낮을수록 일관된 답변
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 20);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content_map));
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            JsonNode response = restTemplate.postForObject(url, requestEntity, JsonNode.class);
            return extractText(response);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            return "요약을 생성하는 중 오류가 발생했습니다.";
        }
    }

    /**
     * keywords + summary를 기반으로 카테고리를 자동 분류한다.
     * 확실하지 않으면 "전체"를 반환한다.
     *
     * @param keywords 키워드 리스트
     * @param summary  생성된 요약문
     * @return 분류된 카테고리 이름 (예: "개발", "경제", "전체")
     */
    public String classifyCategory(List<String> keywords, String summary) {

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                "gemini-1.5-flash:generateContent?key=" + apiKey;

        String keywordsStr = String.join(", ", keywords);

        // ── 프롬프트 ──────────────────────────────────────────────
        // 규칙을 엄격하게 줘서 JSON 외 다른 텍스트가 나오지 않게 함
        String prompt = """
                당신은 웹 링크 카테고리 분류기이다.
                다음 규칙을 반드시 지켜라:
                
                [규칙]
                1. 반드시 아래 JSON 형식만 출력한다. 다른 텍스트는 절대 출력하지 않는다.
                2. category 값은 반드시 아래 목록 중 하나만 사용한다:
                   개발, 디자인, 경제, 뉴스, 과학, 건강, 음식, 여행, 교육, 엔터테인먼트, 전체
                3. "즐겨찾기"는 절대 사용하지 않는다.
                4. 확실하지 않으면 반드시 "전체"를 사용한다.
                5. confidence는 0.0~1.0 사이 숫자이다. 0.7 미만이면 category는 "전체"로 한다.
                
                [출력 형식]
                {"category": "분류명", "confidence": 0.0}
                
                [입력]
                키워드: %s
                요약: %s
                """.formatted(keywordsStr, summary);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("parts", List.of(textPart));

        // temperature를 0으로 설정 → 일관된 JSON 출력 보장
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.0);
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 10);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentMap));
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            JsonNode response = restTemplate.postForObject(url, requestEntity, JsonNode.class);
            return extractCategory(response);
        } catch (Exception e) {
            log.error("카테고리 분류 API 호출 실패: {}", e.getMessage());
            return "전체"; // 실패 시 기본값
        }
    }

    // Gemini 응답 JSON에서 category 값만 꺼낸다
    // 파싱 실패 시 "전체" 반환 (안전장치)
    private String extractCategory(JsonNode response) {
        try {
            // Gemini가 반환한 텍스트: {"category": "개발", "confidence": 0.9}
            String rawText = response
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText("");

            // 텍스트에서 JSON만 파싱
            // (Gemini가 가끔 ```json ... ``` 형태로 감싸서 보내는 경우 대비)
            String cleaned = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode categoryJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(cleaned);

            double confidence = categoryJson.path("confidence").asDouble(0.0);
            String category = categoryJson.path("category").asText("전체");

            // confidence 0.7 미만이면 "전체"로 강제 처리
            if (confidence < 0.7) {
                log.info("카테고리 분류 confidence 낮음 ({}) → '전체' 처리", confidence);
                return "전체";
            }

            // "즐겨찾기"가 나온 경우 강제로 "전체"로 교체 (안전장치)
            if ("즐겨찾기".equals(category)) {
                log.warn("AI가 '즐겨찾기'를 반환함 → '전체'로 교체");
                return "전체";
            }

            log.info("카테고리 분류 결과: {} (confidence: {})", category, confidence);
            return category;

        } catch (Exception e) {
            log.error("카테고리 JSON 파싱 실패: {}", e.getMessage());
            return "전체"; // 파싱 실패 시 기본값
        }
    }

    // JSON 응답에서 텍스트만 꺼내는 헬퍼
    private String extractText(JsonNode response) {
        try {
            return response
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText("요약 결과를 가져올 수 없습니다.");
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            return "요약 결과를 파싱할 수 없습니다.";
        }
    }
}