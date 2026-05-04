package com.project.linkybe_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter // 프론트에서 온 JSON 데이터를 세팅하기 위해 필요!
@NoArgsConstructor // 기본 생성자도 추가해 주는 것이 안전해
public class LinkRequest {
    private String url;

    // 사용자가 직접 입력할 수 있는 제목 (없을 수도 있으니 검증 어노테이션은 생략)
    private String title;

    // 카테고리 (예: "미분류")
    private String category;

    // 공개/비공개 여부 (프론트에서 true/false로 전송)
    private Boolean isPrivate;

    // 프론트에서 toIso8601String()으로 보낸 텍스트를 LocalDateTime으로 자동 변환해 줌
    private LocalDateTime selectedDate;

}
