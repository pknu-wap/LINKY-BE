package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping
    public ApiResponse<?> saveLink(@RequestBody LinkRequest request,
                                   Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        linkService.saveLink(email, request);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<Link>> getLinks(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ApiResponse.success(linkService.getLinks(email));
    }
}
