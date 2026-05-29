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

@Slf4j
@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private static final String DEVICE_UUID_HEADER = "X-Device-UUID";

    private final LinkService linkService;
    private final GeminiService geminiService;

    @PostMapping
    public ApiResponse<LinkResponse> saveLink(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                              @RequestBody LinkRequest request) {
        LinkResponse response = linkService.saveLink(requireDeviceUuid(deviceUuid), request);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{linkId:\\d+}/summary")
    public ApiResponse<LinkResponse> updateLinkSummary(
            @RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
            @PathVariable Long linkId,
            @RequestBody LinkUpdateRequest request) {

        LinkResponse response = linkService.updateLinkSummaryByDevice(
                linkId, requireDeviceUuid(deviceUuid), request.getSummary());
        return ApiResponse.success(response);
    }

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

    @PatchMapping("/{id:\\d+}")
    public ApiResponse<LinkResponse> updateLink(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                                @PathVariable Long id,
                                                @RequestBody LinkUpdateRequest request) {
        return ApiResponse.success(linkService.updateLink(requireDeviceUuid(deviceUuid), id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public ApiResponse<?> deleteLink(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                     @PathVariable Long id) {
        linkService.deleteLink(requireDeviceUuid(deviceUuid), id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/reset")
    public ApiResponse<String> resetLinks(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid) {
        long deletedCount = linkService.resetLinks(requireDeviceUuid(deviceUuid));
        return ApiResponse.success(deletedCount + "개의 링크가 초기화되었습니다.");
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid) {
        byte[] csv = linkService.exportCsv(requireDeviceUuid(deviceUuid));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"links.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @PostMapping("/import")
    public ApiResponse<String> importCsv(@RequestHeader(DEVICE_UUID_HEADER) String deviceUuid,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        int count = linkService.importCsv(requireDeviceUuid(deviceUuid), content);
        return ApiResponse.success(count + "개의 링크를 불러왔습니다.");
    }

    @PostMapping("/summarize")
    public ApiResponse<String> summarizeLink(@RequestParam String url) {
        String summary = geminiService.summarizeUrl(url);
        return ApiResponse.success(summary);
    }

    private String requireDeviceUuid(String deviceUuid) {
        if (deviceUuid == null || deviceUuid.isBlank()) {
            throw CustomException.badRequest(DEVICE_UUID_HEADER + " header is required");
        }
        return deviceUuid.trim();
    }
}
