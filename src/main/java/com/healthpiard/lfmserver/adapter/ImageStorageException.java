/*
 * 파일명: ImageStorageException.java
 * 설명: 이미지 원본 저장소의 처리 실패를 전달한다.
 * 주요기능: 파일시스템 오류의 원인 보존
 * 주의사항: API 계층에서 내부 경로를 노출하지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.adapter;

public class ImageStorageException extends RuntimeException {
    public ImageStorageException(String message) {
        super(message);
    }

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
