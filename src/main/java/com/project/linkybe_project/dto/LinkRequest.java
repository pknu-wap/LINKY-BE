package com.project.linkybe_project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LinkRequest {

    // 빈 문자열(""), 공백(" "), null 값을 모두 막아줍니다.
    @NotBlank(message = "URL은 필수 입력값입니다.")
    private String url;

}