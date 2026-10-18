/*
 * 파일명: TrainingSampleReceipt.java
 * 설명: Feeder 이미지 등록 결과를 표현한다.
 * 주요기능: 요청 식별자, 이미지 해시, 중복 여부, 수집 상태 전달
 * 주의사항: 이미지 등록은 자동 학습 작업을 생성하지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.domain;

public record TrainingSampleReceipt(
        String requestId,
        String imageSha256,
        boolean duplicate,
        String status) {
}
