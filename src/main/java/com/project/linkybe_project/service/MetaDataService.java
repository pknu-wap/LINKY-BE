package com.project.linkybe_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class MetaDataService {

    // application.yml에 등록할 유튜브 API 키
    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    // 1. 메인 판별기: 유튜브 링크인지 일반 링크인지 여기서 결정
    public LinkMetadataDto extractMetadata(String url) {
        if (url != null && (url.contains("youtube.com/watch") || url.contains("youtu.be/"))) {
            return extractYoutubeMetadata(url); // 유튜브면 API 출동
        }
        return extractBasicMetadata(url); // 아니면 기존 크롤링 출동
    }

    // 2. [신규] 유튜브 API 전용 처리 메서드
    private LinkMetadataDto extractYoutubeMetadata(String url) {
        String videoId = extractYoutubeId(url);

        // ID 추출에 실패하면 안전하게 일반 크롤링
        if (videoId == null) return extractBasicMetadata(url);

        String apiUrl = "https://www.googleapis.com/youtube/v3/videos?id=" + videoId + "&key=" + youtubeApiKey + "&part=snippet";
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 구글 서버에 요청 보내서 데이터 받아오기
            String response = restTemplate.getForObject(apiUrl, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("items");

            if (items.isArray() && !items.isEmpty()) {
                JsonNode snippet = items.get(0).path("snippet");

                String title = snippet.path("title").asText();
                String channelName = snippet.path("channelTitle").asText();
                String description = "📺 " + channelName + " 채널의 영상입니다."; // 요약에 채널명 쏙!
                String imageUrl = snippet.path("thumbnails").path("high").path("url").asText(); // 고화질 썸네일

                return new LinkMetadataDto(title, imageUrl, description, url);
            }
        } catch (Exception e) {
            System.out.println("유튜브 API 호출 실패: " + e.getMessage());
        }

        // API 통신 중 에러가 나면 앱이 터지지 않도록 일반 크롤링으로 백업 처리
        return extractBasicMetadata(url);
    }

    // 3. [기존] 작성해두신 일반 웹사이트 크롤링 메서드 (이름만 바꿈)
    private LinkMetadataDto extractBasicMetadata(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    // 403 에러 방지를 위해 크롬 브라우저인 것처럼 더 강력하게 위장
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(5000)
                    .get();

            String title = getMetaTagContent(doc, "og:title");
            String imageUrl = getMetaTagContent(doc, "og:image");
            String description = getMetaTagContent(doc, "og:description");

            if (title == null || title.isBlank()) {
                title = doc.title();
            }

            return new LinkMetadataDto(title, imageUrl, description, url);

        } catch (IOException e) {
            System.out.println("메타데이터 수집 실패: " + e.getMessage());
            return new LinkMetadataDto("제목 없음", null, "정보를 불러올 수 없습니다.", url);
        }
    }

    // [기존] 헬퍼 메서드: <meta> 태그 파싱
    private String getMetaTagContent(Document doc, String property) {
        Element metaTag = doc.selectFirst("meta[property=" + property + "]");
        return metaTag != null ? metaTag.attr("content") : null;
    }

    // [신규] 헬퍼 메서드: 유튜브 URL에서 v= 뒤에 있는 고유 ID만 잘라내기
    private String extractYoutubeId(String url) {
        try {
            if (url.contains("youtube.com/watch?v=")) {
                return url.split("v=")[1].split("&")[0];
            } else if (url.contains("youtu.be/")) {
                return url.split("youtu.be/")[1].split("\\?")[0];
            }
        } catch (Exception e) {
            System.out.println("유튜브 ID 추출 실패: " + url);
        }
        return null;
    }

    // DTO
    public record LinkMetadataDto(String title, String imageUrl, String description, String originalUrl) {
    }
}