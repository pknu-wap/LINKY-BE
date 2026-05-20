package com.project.linkybe_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter // 프론트에서 온 JSON 데이터를 세팅하기 위해 필요
@NoArgsConstructor // 기본 생성자도 추가해 주는 것이 안전
public class LinkRequest {

    private String url;             // 필수
    private String title;           // 선택 (없으면 메타데이터로 채워짐)
    private String category;        // 선택 (없으면 null)
    private Boolean isPrivate;      // 선택 (없으면 null → false 처리)
    private LocalDateTime selectedDate; // 선택 (리마인더 날짜)
    private String summary;
}
