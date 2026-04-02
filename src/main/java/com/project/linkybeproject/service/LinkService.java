package com.project.linkybeproject.service;

import com.project.linkybeproject.domain.Link;
import com.project.linkybeproject.dto.LinkRequest;
import com.project.linkybeproject.repository.LinkRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service // 이 클래스가 비즈니스 로직을 처리하는 '서비스'임을 선언합니다.
public class LinkService {

    private final LinkRepository linkRepository;

    // Repository(DB 창구)를 연결해 줍니다.
    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    // 1. 링크 저장 기능
    public Link saveLink(LinkRequest request) {
        Link link = new Link();
        link.setUrl(request.getUrl());

        // (참고: 대연님이 메타데이터 수집 기능을 완성하면 여기에 코드가 추가될 예정입니다!)

        return linkRepository.save(link); // DB에 저장!
    }

    // 2. 링크 목록 조회 기능
    public List<Link> getAllLinks() {
        return linkRepository.findAll(); // DB에서 모두 꺼내오기!
    }
}