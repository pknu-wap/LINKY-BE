package com.project.linkybe_project.controller;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.LinkRepository;
import com.project.linkybe_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<?> saveLink(@RequestBody Link link, HttpServletRequest request) {

        String email = (String) request.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        link.assignUser(user);

        linkRepository.save(link);

        return ApiResponse.success(null);
    }
}