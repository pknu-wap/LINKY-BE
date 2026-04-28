package com.project.linkybeproject.common;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String error;

    // 생성자
    public ApiResponse(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    // 1. 성공했을 때 응답을 쉽게 만들어주는 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 2. 실패했을 때 에러 메시지를 보내주는 메서드
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }

    // Getter (데이터를 읽을 수 있게 해줌)
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getError() { return error; }
}