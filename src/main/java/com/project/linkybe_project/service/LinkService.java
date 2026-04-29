package com.project.linkybe_project.service;

import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.LinkRepository;
import com.project.linkybe_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    public void saveLink(String kakaoId, LinkRequest request) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Link link = new Link();
        link.setUrl(request.getUrl());
        link.assignUser(user);

        linkRepository.save(link);
    }

    public List<Link> getLinks(String kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return linkRepository.findByUserOrderByIdDesc(user);
    }
}
