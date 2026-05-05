package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.dto.LinkUpdateRequest;
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
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    // POST /links — 링크 저장
    @PostMapping
    public ApiResponse<LinkResponse> saveLink(@RequestBody LinkRequest request,
                                              Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();
        return ApiResponse.success(linkService.saveLink(kakaoId, request));
    }

    // GET /links                — 전체 조회 (본인 링크만)
    // GET /links?category=개발  — 카테고리 필터
    // GET /links?keyword=유튜브  — 키워드 검색
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
}
