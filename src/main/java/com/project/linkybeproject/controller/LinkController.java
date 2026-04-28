package com.project.linkybeproject.controller;

import com.project.linkybeproject.common.ApiResponse;
import com.project.linkybeproject.domain.Link;
import com.project.linkybeproject.dto.LinkRequest;
import com.project.linkybeproject.service.LinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/links") // 기본 주소를 '/links'로 설정합니다.
public class LinkController {

    private final LinkService linkService;

    // Service(두뇌)를 연결해 줍니다.
    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    // 1. 링크 저장 API (대연님의 'POST /links' 규칙 적용)
    @PostMapping
    public ApiResponse<Link> createLink(@RequestBody LinkRequest request) {
        Link savedLink = linkService.saveLink(request);
        return ApiResponse.success(savedLink); // 1단계에서 만든 공통 폼으로 예쁘게 포장해서 응답!
    }

    // 2. 링크 목록 조회 API (대연님의 'GET /links' 규칙 적용)
    @GetMapping
    public ApiResponse<List<Link>> getLinks() {
        List<Link> links = linkService.getAllLinks();
        return ApiResponse.success(links);
    }

    // 3. 링크 상세 조회 API (예: GET /links/1)
    @GetMapping("/{id}")
    public ApiResponse<Link> getLink(@PathVariable Long id) {
        Link link = linkService.getLink(id);
        return ApiResponse.success(link);
    }

    // 4. 링크 삭제 API (예: DELETE /links/1)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
        return ApiResponse.success(null); // 삭제는 돌려줄 데이터가 없으므로 null을 보냅니다.
    }
}