//백엔드에서 에러가 났을 때 응답 형식을 말해주는 파일

package com.project.linkybe_project.config;

import com.project.linkybe_project.dto.ApiResponse;
import com.project.linkybe_project.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 컨트롤러에서 발생한 예외를 공통 응답 형식으로 변환함.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 아래 코드는 의도적으로 발생시키는 에러를 처리하는 코드임.
    // 400, 404, 403 에러 보냄 (순서대로 잘못된 요청, 없는 링크, 권한 문제)
    // CustomException의 상태 코드와 메시지를 그대로 flutter에 전달함.
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        log.warn("CustomException [{}]: {}", e.getStatus(), e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(e.getMessage()));
    }

    // 아래 코드는 예상하지 못한 서버 에러를 처리하는 코드임.
    // 예상하지 못한 런타임 예외는 500 응답으로 변환함.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: ", e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}
