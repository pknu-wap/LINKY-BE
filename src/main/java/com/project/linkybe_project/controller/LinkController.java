package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/links") // API 기본 경로: /links
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    // ───────────────────────────────────────────────
    // 링크 저장 (수정됨: null 대신 저장된 결과 반환)
    // ───────────────────────────────────────────────
    @PostMapping
    public ApiResponse<LinkResponse> saveLink_DB(@RequestBody LinkRequest request, Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();

        // Service에서 반환한 저장 결과를 변수에 담습니다.
        LinkResponse response = linkService.saveLink_DB(kakaoId, request);

        // 프론트엔드에서 바로 확인할 수 있도록 response 객체를 넘겨줌.
        return ApiResponse.success(response);
    }

    // ───────────────────────────────────────────────
    // 사용자 요약 직접 수정 (새로 추가된 API)
    // ───────────────────────────────────────────────
    @PatchMapping("/{linkId}/summary")
    public ApiResponse<LinkResponse> updateLinkSummary(
            @PathVariable Long linkId,
            @RequestBody LinkRequest request,
            Authentication authentication) {

        // 1. Security Context에서 유저의 카카오 ID 추출
        String kakaoId = (String) authentication.getPrincipal();

        // 2. Service 호출하여 요약 수정 로직 실행 (DTO에서 사용자가 입력한 summary 추출)
        LinkResponse response = linkService.updateLinkSummaryByUser(linkId, kakaoId, request.getSummary());

        // 3. 성공 응답 반환
        return ApiResponse.success(response);
    }

    // ───────────────────────────────────────────────
    // GET /links — 전체 링크 조회
    // ───────────────────────────────────────────────
    @GetMapping
    public ApiResponse<List<LinkResponse>> getLinks(Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();
        return ApiResponse.success(linkService.getLinks(kakaoId));
    }
}