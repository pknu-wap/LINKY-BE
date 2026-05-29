package com.project.linkybe_project.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("if_favorite")
    @JsonAlias({"ifFavorite", "is_favorite", "isFavorite"})
    private Boolean ifFavorite;
    private String memo;

    public String getSummary() {
        return this.getSummary;

    }
}
