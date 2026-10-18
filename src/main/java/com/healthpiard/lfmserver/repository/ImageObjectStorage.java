/*
 * 파일명: ImageObjectStorage.java
 * 설명: 이미지 원본 저장소의 도메인 포트이다.
 * 주요기능: 해시 기반 불변 이미지 저장
 * 주의사항: 구현체는 동일 바이트를 중복 저장하지 않아야 한다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.repository;

import com.healthpiard.lfmserver.domain.StoredImage;

public interface ImageObjectStorage {
    StoredImage store(byte[] imageBytes, String contentType);
}
