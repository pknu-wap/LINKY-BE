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

    // LinkRequest에 있던 필드들도 프론트엔드로 다시 보내주기 위해 추가
    private final Boolean isPrivate;
    private final LocalDateTime selectedDate;

    private final LocalDateTime createdAt;

    // 요약 데이터 필드 추가
    private final String summary;

    public LinkResponse(Link link) {
        this.id = link.getId();
        this.url = link.getUrl();
        this.title = link.getTitle();
        this.category = link.getCategory();

        // 추가된 필드들 매핑
        this.isPrivate = link.getIsPrivate();
        this.selectedDate = link.getSelectedDate();

        this.createdAt = link.getCreatedAt();

        // DB에서 꺼내온 요약 내용을 DTO에 담아줌
        this.summary = link.getSummary();
    }
}