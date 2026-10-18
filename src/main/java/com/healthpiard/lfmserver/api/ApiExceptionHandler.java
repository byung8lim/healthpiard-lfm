/*
 * 파일명: ApiExceptionHandler.java
 * 설명: API 입력 및 저장 오류를 일관된 JSON 응답으로 변환한다.
 * 주요기능: 검증 오류 400, 저장 오류 500 처리
 * 주의사항: 예외를 숨기지 않고 서버 오류는 로그에 원인을 남긴다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.api;

import com.healthpiard.lfmserver.adapter.ImageStorageException;
import com.healthpiard.lfmserver.service.InvalidTrainingSampleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidTrainingSampleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTrainingSample(InvalidTrainingSampleException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> handleMissingRequestPart(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "REQUIRED_PART_MISSING", exception.getMessage());
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<Map<String, Object>> handleImageStorage(ImageStorageException exception) {
        log.error("이미지 저장소 처리 실패", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_STORAGE_FAILED", "이미지 저장에 실패했습니다.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "success", false,
                "detail", Map.of("code", code, "message", message)));
    }
}
