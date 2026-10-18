/*
 * 파일명: IngestProperties.java
 * 설명: Feeder 이미지 수집 설정을 외부 환경설정과 연결한다.
 * 주요기능: 저장소 루트와 최대 이미지 크기 제공
 * 주의사항: 운영 경로와 제한값을 소스에 하드코딩하지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "healthpiard.ingest")
public record IngestProperties(Path storageRoot, long maxImageSizeBytes) {
    public IngestProperties {
        if (storageRoot == null) {
            throw new IllegalArgumentException("이미지 저장소 경로가 필요합니다.");
        }
        if (maxImageSizeBytes <= 0) {
            throw new IllegalArgumentException("최대 이미지 크기는 0보다 커야 합니다.");
        }
    }
}
