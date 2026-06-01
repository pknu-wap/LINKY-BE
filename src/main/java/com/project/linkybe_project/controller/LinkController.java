// /links로 들어오는 HTTP 요청을 받는 파일
// 요청/응답 연결을 담당하는 파일
package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.dto.LinkResponse;
import com.project.linkybe_project.dto.LinkUpdateRequest;
import com.project.linkybe_project.exception.CustomException;
import com.project.linkybe_project.service.GeminiService;
import com.project.linkybe_project.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// 링크 저장, 조회, 수정, 삭제, 가져오기, 내보내기 API를 제공함.
@Slf4j
@RestController
//기본 주소
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {
    // 기기별 데이터를 구분하기 위해 사용하는 요청 헤더 이름임.
    private static final String DEVICE_UUID_HEADER = "X-Device-UUID";

    // 링크 도메인 로직을 처리하는 서비스
    private final LinkService linkService;

    // URL 요약을 직접 요청할 때 사용하는 Gemini 서비스
    private final GeminiService geminiService;

    // 새 링크를 저장함.
    @PostMapping
    public ApiResponse<LinkResponse> saveLink(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                              @RequestBody LinkRequest request) {
        LinkResponse response = linkService.saveLink(requireDeviceUuid(deviceUuid), request);
        return ApiResponse.success(response);
    }

    // 특정 링크의 요약 내용을 직접 수정함.
    @PatchMapping("/{linkId:\\d+}/summary")
    public ApiResponse<LinkResponse> updateLinkSummary(
            @RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
            @PathVariable Long linkId,
            @RequestBody LinkUpdateRequest request) {

        LinkResponse response = linkService.updateLinkSummaryByDevice(
                linkId, requireDeviceUuid(deviceUuid), request.getSummary());
        return ApiResponse.success(response);
    }

    // 전체 목록, 카테고리 목록, 키워드 검색 결과 중 조건에 맞는 링크 목록을 반환함.
    @GetMapping
    public ApiResponse<List<LinkResponse>> getLinks(
            @RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        String resolvedDeviceUuid = requireDeviceUuid(deviceUuid);

        if (keyword != null && !keyword.isBlank()) {
            return ApiResponse.success(linkService.searchLinks(resolvedDeviceUuid, keyword));
        }
        if (category != null && !category.isBlank()) {
            return ApiResponse.success(linkService.getLinksByCategory(resolvedDeviceUuid, category));
        }
        return ApiResponse.success(linkService.getLinks(resolvedDeviceUuid));
    }

    // 특정 링크의 수정 가능한 필드를 갱신함.
    @PatchMapping("/{id:\\d+}")
    public ApiResponse<LinkResponse> updateLink(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                                @PathVariable Long id,
                                                @RequestBody LinkUpdateRequest request) {
        return ApiResponse.success(linkService.updateLink(requireDeviceUuid(deviceUuid), id, request));
    }

    // 특정 링크 하나를 삭제함.
    @DeleteMapping("/{id:\\d+}")
    public ApiResponse<?> deleteLink(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                     @PathVariable Long id) {
        linkService.deleteLink(requireDeviceUuid(deviceUuid), id);
        return ApiResponse.success(null);
    }

    // 현재 기기의 모든 링크를 삭제함.
    @DeleteMapping("/reset")
    public ApiResponse<String> resetLinks(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid) {
        long deletedCount = linkService.resetLinks(requireDeviceUuid(deviceUuid));
        return ApiResponse.success(deletedCount + "개의 링크가 초기화되었습니다.");
    }

    // 현재 기기의 링크 목록을 CSV 파일로 내려줌.
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid) {
        byte[] csv = linkService.exportCsv(requireDeviceUuid(deviceUuid));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"links.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    // 업로드된 CSV 파일을 읽어 링크 목록으로 저장함.
    @PostMapping("/import")
    public ApiResponse<String> importCsv(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        int count = linkService.importCsv(requireDeviceUuid(deviceUuid), content);
        return ApiResponse.success(count + "개의 링크를 불러왔습니다.");
    }

    // URL 하나를 즉시 요약해 문자열로 반환함.
    @PostMapping("/summarize")
    public ApiResponse<String> summarizeLink(@RequestParam String url) {
        String summary = geminiService.summarizeUrl(url);
        return ApiResponse.success(summary);
    }

    // 필수 기기 UUID 헤더를 검증하고 앞뒤 공백을 제거함.
    private String requireDeviceUuid(String deviceUuid) {
        if (deviceUuid == null || deviceUuid.isBlank()) {
            throw CustomException.badRequest(DEVICE_UUID_HEADER + " header is required");
        }
        return deviceUuid.trim();
    }
}
