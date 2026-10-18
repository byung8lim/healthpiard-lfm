/*
 * 파일명: TrainingSampleCommand.java
 * 설명: 이미지 수집 유스케이스의 입력을 표현한다.
 * 주요기능: 이미지 바이트와 Feeder 요청 메타데이터 전달
 * 주의사항: 외부 multipart 타입이 도메인 계층으로 유입되지 않게 한다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.domain;

import java.time.Instant;

public record TrainingSampleCommand(
        byte[] imageBytes,
        String originalFilename,
        String contentType,
        FoodMetadata foodMetadata,
        String clientRequestId,
        String deviceId,
        Instant capturedAt) {
}
