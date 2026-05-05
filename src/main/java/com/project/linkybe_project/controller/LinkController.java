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
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    // ───────────────────────────────────────────────
    @PostMapping
    public ApiResponse<LinkResponse> saveLink_DB(@RequestBody LinkRequest request, Authentication authentication) {
        String kakaoId = (String) authentication.getPrincipal();
        linkService.saveLink_DB(kakaoId, request);
        return ApiResponse.success(null);
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
