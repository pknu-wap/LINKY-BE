package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.LinkRepository;
import com.project.linkybe_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<?> saveLink(@RequestBody LinkRequest request,
                                   Authentication authentication) {

        String email = (String) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Link link = new Link();
        link.setUrl(request.getUrl());
        link.assignUser(user);

        linkRepository.save(link);

        return ApiResponse.success(null);
    }
}