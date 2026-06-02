package com.project.linkybe_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 링크 수정 요청에서 변경 가능한 값을 담는다.
@Getter
@Setter
@NoArgsConstructor
public class LinkUpdateRequest {

    // 직접 수정할 요약 내용을 보관하는 필드
    String getSummary;

    // 변경할 링크 제목
    private String title;

    // 변경할 링크 카테고리
    private String category;

    // 변경할 비공개 여부
    private Boolean isPrivate;

    // 변경할 선택 날짜 또는 리마인더 날짜
    private LocalDateTime selectedDate;

    private Boolean clearSelectedDate;

    // JSON의 isFavorite 값을 즐겨찾기 여부로 매핑함
    @JsonProperty("isFavorite")
    private Boolean isFavorite;

    // 클라이언트가 전달할 수 있는 메모 값
    private String memo;

    // 직접 수정할 요약 내용을 반환함.
    public String getSummary() {
        return this.getSummary;

    }
}
