package com.project.linkybe_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LinkUpdateRequest {

    String getSummary;
    private String title;
    private String category;
    private Boolean isPrivate;
    private LocalDateTime selectedDate;
    private String memo;

    public String getSummary() {
        return this.getSummary;

    }
}
