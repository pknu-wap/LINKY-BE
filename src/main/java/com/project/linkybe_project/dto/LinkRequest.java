package com.project.linkybe_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LinkRequest {

    private String url;             // 필수
    private String title;           // 선택 (없으면 메타데이터로 채워짐)
    private String category;        // 선택 (없으면 null)
    private Boolean isPrivate;      // 선택 (없으면 null → false 처리)
    private LocalDateTime selectedDate; // 선택 (리마인더 날짜)
    private String memo;            // 선택 (사용자 메모)
}
