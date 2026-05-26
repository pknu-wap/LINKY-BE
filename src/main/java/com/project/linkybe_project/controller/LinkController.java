package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.dto.LinkUpdateRequest;
import com.project.linkybe_project.service.GeminiService;
import com.project.linkybe_project.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/links") // API 기본 경로: /links
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;
    private final GeminiService geminiService;

    // POST /links — 링크 저장
    @PostMapping
    public ApiResponse<LinkResponse> saveLink(@RequestBody LinkRequest request,
                                              Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();

        // Service에서 반환한 저장 결과를 변수에 담습니다.
        LinkResponse response = linkService.saveLink(kakaoId, request);

        // 프론트엔드에서 바로 확인할 수 있도록 response 객체를 넘겨줌.
        return ApiResponse.success(response);
    }

    // ───────────────────────────────────────────────
    // 사용자 요약 직접 수정 (새로 추가된 API)
    // ───────────────────────────────────────────────
    @PatchMapping("/{linkId}/summary")
    public ApiResponse<LinkResponse> updateLinkSummary(
            @PathVariable Long linkId,
            @RequestBody LinkUpdateRequest request,
            Authentication authentication) {

        // 1. Security Context에서 유저의 카카오 ID 추출
        String kakaoId = (String) authentication.getPrincipal();

        // 2. Service 호출하여 요약 수정 로직 실행 (DTO에서 사용자가 입력한 summary 추출)
        LinkResponse response = linkService.updateLinkSummaryByUser(linkId, kakaoId, request.getSummary());

        // 3. 성공 응답 반환
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<LinkResponse>> getLinks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Authentication authentication) {

        String kakaoId = (String) authentication.getPrincipal();

        if (keyword != null && !keyword.isBlank()) {
            return ApiResponse.success(linkService.searchLinks(kakaoId, keyword));
        }
        if (category != null && !category.isBlank()) {
            return ApiResponse.success(linkService.getLinksByCategory(kakaoId, category));
        }
        return ApiResponse.success(linkService.getLinks(kakaoId));
    }

    // PATCH /links/{id} — 링크 수정 (본인 링크만)
    @PatchMapping("/{id}")
    public ApiResponse<LinkResponse> updateLink(@PathVariable Long id,
                                                @RequestBody LinkUpdateRequest request,
                                                Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();
        return ApiResponse.success(linkService.updateLink(kakaoId, id, request));
    }

    // DELETE /links/{id} — 링크 삭제 (본인 링크만, user+id 동시 검증)
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteLink(@PathVariable Long id,
                                     Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();
        linkService.deleteLink(kakaoId, id);
        return ApiResponse.success(null);
    }

    // GET /links/export — CSV 다운로드
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();
        byte[] csv = linkService.exportCsv(kakaoId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"links.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    // POST /links/import — CSV 업로드 (form-data: file=links.csv)
    @PostMapping("/import")
    public ApiResponse<String> importCsv(@RequestParam("file") MultipartFile file,
                                         Authentication authentication) throws IOException {
        String kakaoId = (String) authentication.getPrincipal();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        int count = linkService.importCsv(kakaoId, content);
        return ApiResponse.success(count + "개의 링크를 불러왔습니다.");
    }

    // POST /links/summarize — AI 링크 요약
    @PostMapping("/summarize")
    public ApiResponse<String> summarizeLink(@RequestParam String url,
                                             Authentication authentication) {
        // 인증된 사용자만 사용 가능 (authentication 객체로 검증됨)
        String summary = geminiService.summarizeUrl(url);
        return ApiResponse.success(summary);
    }
}
