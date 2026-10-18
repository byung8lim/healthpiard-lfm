/*
 * 파일명: FoodMetadata.java
 * 설명: Feeder가 전송한 음식 및 영양정보의 내부 표현이다.
 * 주요기능: 필수 음식명과 원본 메타데이터 보존
 * 주의사항: Feeder의 JSON Key를 변경하거나 손실시키지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record FoodMetadata(String foodName, JsonNode rawMetadata) {
}
