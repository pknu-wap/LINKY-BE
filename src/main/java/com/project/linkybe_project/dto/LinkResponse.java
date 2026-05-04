package com.project.linkybe_project.dto;

import com.project.linkybe_project.entity.Link;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LinkResponse {
    private final Long id;
    private final String url;
    private final String title;
    private final String siteName;
    private final String thumbnailUrl;
    private final String memo;
    private final String category;
    private final LocalDateTime createdAt;

    public LinkResponse(Link link) {
        this.id = link.getId();
        this.url = link.getUrl();
        this.title = link.getTitle();
        this.siteName = link.getSiteName();
        this.thumbnailUrl = link.getThumbnailUrl();
        this.memo = link.getMemo();
        this.category = link.getCategory();
        this.createdAt = link.getCreatedAt();
    }
}
