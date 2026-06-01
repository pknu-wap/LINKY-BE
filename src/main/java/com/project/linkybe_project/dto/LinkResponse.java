package com.project.linkybe_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.SummaryStatus;
import lombok.Getter;

import java.time.LocalDateTime;

// 링크 조회와 저장 결과를 클라이언트에 반환하는 DTO임.
@Getter
public class LinkResponse {

    // 링크의 고유 식별자
    private final Long id;

    // 저장된 원본 링크 URL
    private final String url;

    // 링크 제목
    private final String title;

    // 링크 카테고리
    private final String category;

    // 링크 비공개 여부
    private final Boolean isPrivate;

    // 링크와 연결된 선택 날짜 또는 리마인더 날짜
    private final LocalDateTime selectedDate;

    // JSON 응답에서 isFavorite 이름으로 내려갈 즐겨찾기 여부
    @JsonProperty("isFavorite")
    private final Boolean isFavorite;

    // 링크 요약 내용
    private final String summary;

    // 요약 생성의 현재 처리 상태
    private final SummaryStatus summaryStatus;

    // Link 엔티티를 API 응답 DTO로 변환함.
    public LinkResponse(Link link) {
        this.id = link.getId();
        this.url = link.getUrl();
        this.title = link.getTitle();
        this.category = link.getCategory();
        this.isPrivate = link.getIsPrivate();
        this.selectedDate = link.getSelectedDate();
        this.isFavorite = link.getIsFavorite() != null ? link.getIsFavorite() : false;
        this.summary = link.getSummary();
        this.summaryStatus = link.getSummaryStatus();
    }
}
