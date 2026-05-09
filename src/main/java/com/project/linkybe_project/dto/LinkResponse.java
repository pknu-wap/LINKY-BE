package com.project.linkybe_project.dto;

import com.project.linkybe_project.entity.Link;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LinkResponse {

    private final Long id;
    private final String url;
    private final String title;
    private final String category;
    private final Boolean isPrivate;
    private final LocalDateTime selectedDate;
    private final String summary;  // 생성자에서 link.getSummary() 로 받음

    // Link 엔티티에서 필요한 필드만 추출 — user, refreshToken 등 민감 정보 노출 차단
    public LinkResponse(Link link) {
        this.id = link.getId();
        this.url = link.getUrl();
        this.title = link.getTitle();
        this.category = link.getCategory();
        this.isPrivate = link.getIsPrivate();
        this.selectedDate = link.getSelectedDate();
        this.summary = link.getSummary();
        // user 필드는 의도적으로 포함하지 않음
    }
}
