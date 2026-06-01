package com.project.linkybe_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 모든 API 응답을 성공 여부, 데이터, 오류 메시지 구조로 감쌈.
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    // 요청 처리 성공 여부를 나타냄.
    private boolean success;

    // 성공 응답에서 반환할 실제 데이터임.
    private T data;

    // 실패 응답에서 반환할 오류 메시지임.
    private String error;

    // 성공 응답 객체를 생성함.
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 실패 응답 객체를 생성함.
    public static ApiResponse<?> error(String error) {
        return new ApiResponse<>(false, null, error);
    }
}
