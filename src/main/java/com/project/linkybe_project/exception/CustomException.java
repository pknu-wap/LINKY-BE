package com.project.linkybe_project.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final int status;

    public CustomException(int status, String message) {
        super(message);
        this.status = status;
    }

    // 자주 쓰는 상태코드 팩토리 메서드
    public static CustomException notFound(String message) {
        return new CustomException(404, message);
    }

    public static CustomException unauthorized(String message) {
        return new CustomException(401, message);
    }

    public static CustomException forbidden(String message) {
        return new CustomException(403, message);
    }

    public static CustomException badRequest(String message) {
        return new CustomException(400, message);
    }
}
