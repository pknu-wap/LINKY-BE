package com.project.linkybe_project.dto;

import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.SummaryStatus;
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
    private final String summary;
    private final SummaryStatus summaryStatus;

    public LinkResponse(Link link) {
        this.id = link.getId();
        this.url = link.getUrl();
        this.title = link.getTitle();
        this.category = link.getCategory();
        this.isPrivate = link.getIsPrivate();
        this.selectedDate = link.getSelectedDate();
        this.summary = link.getSummary();
        this.summaryStatus = link.getSummaryStatus();
    }
}
