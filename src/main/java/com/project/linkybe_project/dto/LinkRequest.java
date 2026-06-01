package com.project.linkybe_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// 링크 생성 요청에서 flutter가 전달하는 값을 담음.
@Getter
@Setter // 프론트에서 온 JSON 데이터를 세팅하기 위해 필요
@NoArgsConstructor // 기본 생성자도 추가해 주는 것이 안전
public class LinkRequest {

    // 저장할 원본 링크 URL임.
    private String url;             // 필수

    // flutter가 지정한 링크 제목임.
    // 선택 (없으면 ai가 제목 채워줌)
    private String title;

    // flutter가 지정한 대표 카테고리임.
    // 선택 (없으면 null) -> ai가 분류해줌
    private String category;

    // 링크를 비공개로 표시할지 여부임.
    // 선택 (없으면 null → false 처리)
    private Boolean isPrivate;

    // 링크와 연결된 선택 날짜 또는 리마인더 날짜임.
    // 선택 (리마인더 날짜)
    private LocalDateTime selectedDate;

    // JSON의 isFavorite 값을 즐겨찾기 여부로 매핑함.
    @JsonProperty("isFavorite")
    private Boolean isFavorite;

    // 자동 카테고리 분류에 사용할 후보 목록임.
    private List<String> categories;

    // flutter가 전달할 수 있는 요약 문구임.
    private String summary;
}
