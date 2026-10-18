/*
 * 파일명: InvalidTrainingSampleException.java
 * 설명: Feeder 입력 검증 실패를 오류 코드와 함께 전달한다.
 * 주요기능: 호환 API 오류 코드 보존
 * 주의사항: 사용자 입력 오류와 서버 저장 오류를 구분한다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.service;

public class InvalidTrainingSampleException extends RuntimeException {
    private final String code;

    public InvalidTrainingSampleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
