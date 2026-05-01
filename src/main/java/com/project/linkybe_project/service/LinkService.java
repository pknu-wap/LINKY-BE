package com.project.linkybe_project.service;

import com.project.linkybe_project.dto.LinkRequest;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import com.project.linkybe_project.repository.LinkRepository;
import com.project.linkybe_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final MetaDataService metaDataService;

    @Transactional
    public void saveLink(String email, LinkRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        MetaDataService.LinkMetadataDto metadata = metaDataService.extractMetadata(request.getUrl());

        Link link = new Link();
        link.setUrl(request.getUrl());
        link.assignUser(user);

        link.setTitle(metadata.title());

        linkRepository.save(link);
    }

    @Transactional
    public List<Link> getLinks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return linkRepository.findByUserOrderByIdDesc(user);
    }
}
