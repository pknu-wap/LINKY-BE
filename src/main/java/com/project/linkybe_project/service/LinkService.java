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
        //유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        //외부 API 및 Jsoup으로 메타데이터 추출
        MetaDataService.LinkMetadataDto metadata = metaDataService.extractMetadata(request.getUrl());
        //link 엔티티 생성, 기본 데이터 세팅
        Link link = new Link();
        //프론터에서 받아온 기본 정보 세팅
        link.setUrl(request.getUrl());
        link.assignUser(user);
        //크롤링해온 메타데이터 정보 세팅
        link.setTitle(metadata.title());
        //프론트엔드에서 넘어온 추가 정보 세팅
        link.setCategory(request.getCategory());
        link.setIsPrivate(request.getIsPrivate());
        link.setSelectedDate(request.getSelectedDate());

        //제목 세팅
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            link.setTitle(request.getTitle());
        } else {
            link.setTitle(metadata.title());
        }

        //DB 저장이요 하하하
        linkRepository.save(link);
    }

    @Transactional
    public List<Link> getLinks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return linkRepository.findByUserOrderByIdDesc(user);
    }
}
