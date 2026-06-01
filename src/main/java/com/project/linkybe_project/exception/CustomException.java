package com.project.linkybe_project.exception;

import lombok.Getter;

// HTTP 상태 코드와 메시지를 함께 담는 비즈니스 예외
@Getter
public class CustomException extends RuntimeException {

    // flutter에 반환할 HTTP 상태 코드
    private final int status;

    // 상태 코드와 메시지로 예외를 생성
    public CustomException(int status, String message) {
        super(message);
        this.status = status;
    }

    // 404 Not Found 예외를 생성
    public static CustomException notFound(String message) {
        return new CustomException(404, message);
    }

    // 401 유효한 인증 정보를 제공하지 않음 예외를 생성
    public static CustomException unauthorized(String message) {
        return new CustomException(401, message);
    }

    // 403 접근 권한이 없어 요청을 거부 예외를 생성
    public static CustomException forbidden(String message) {
        return new CustomException(403, message);
    }

    // 400 사용자의 요청이 잘못된 형식으로 서버에 전달 예외를 생성
    public static CustomException badRequest(String message) {
        return new CustomException(400, message);
    }
}
