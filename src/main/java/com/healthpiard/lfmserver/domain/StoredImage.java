/*
 * 파일명: StoredImage.java
 * 설명: 불변 이미지 저장 결과를 표현한다.
 * 주요기능: SHA-256, 저장 키, 중복 여부 전달
 * 주의사항: 저장 키는 외부 절대경로를 노출하지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.domain;

public record StoredImage(String sha256, String storageKey, long sizeBytes, boolean duplicate) {
}
