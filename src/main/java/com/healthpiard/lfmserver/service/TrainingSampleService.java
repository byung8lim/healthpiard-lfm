/*
 * 파일명: TrainingSampleService.java
 * 설명: Feeder 학습 샘플 등록 유스케이스를 수행한다.
 * 주요기능: 입력 검증, 이미지 불변 저장, 등록 영수증 생성
 * 주의사항: 장수 기준 도달 여부와 무관하게 자동 학습을 시작하지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.service;

import com.healthpiard.lfmserver.config.IngestProperties;
import com.healthpiard.lfmserver.domain.StoredImage;
import com.healthpiard.lfmserver.domain.TrainingSampleCommand;
import com.healthpiard.lfmserver.domain.TrainingSampleReceipt;
import com.healthpiard.lfmserver.repository.ImageObjectStorage;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrainingSampleService {
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private final ImageObjectStorage imageObjectStorage;
    private final IngestProperties ingestProperties;

    public TrainingSampleService(ImageObjectStorage imageObjectStorage, IngestProperties ingestProperties) {
        this.imageObjectStorage = imageObjectStorage;
        this.ingestProperties = ingestProperties;
    }

    /*
     * 기능: Feeder가 보낸 학습 샘플을 등록한다.
     * 처리절차: 필수값과 형식을 검증한 뒤 SHA-256 기반 저장소에 원본을 보관한다.
     * 입력: 이미지와 음식 메타데이터를 포함한 등록 명령
     * 출력: 요청 ID, 이미지 해시, 중복 여부를 포함한 등록 결과
     * 예외: 필수값 누락, 허용하지 않는 형식, 크기 초과 시 입력 예외
     */
    public TrainingSampleReceipt registerSample(TrainingSampleCommand command) {
        validateCommand(command);
        StoredImage storedImage = imageObjectStorage.store(command.imageBytes(), command.contentType());
        return new TrainingSampleReceipt(
                command.clientRequestId(), storedImage.sha256(), storedImage.duplicate(), "ACCEPTED");
    }

    private void validateCommand(TrainingSampleCommand command) {
        if (command.imageBytes() == null || command.imageBytes().length == 0) {
            throw new InvalidTrainingSampleException("EMPTY_IMAGE", "이미지 파일이 비어 있습니다.");
        }
        if (command.imageBytes().length > ingestProperties.maxImageSizeBytes()) {
            throw new InvalidTrainingSampleException("IMAGE_TOO_LARGE", "이미지 파일이 허용 크기를 초과했습니다.");
        }
        if (!SUPPORTED_CONTENT_TYPES.contains(command.contentType())) {
            throw new InvalidTrainingSampleException("UNSUPPORTED_IMAGE_TYPE", "지원하지 않는 이미지 형식입니다.");
        }
        if (command.foodMetadata() == null || command.foodMetadata().foodName() == null
                || command.foodMetadata().foodName().isBlank()) {
            throw new InvalidTrainingSampleException("FOOD_NAME_REQUIRED", "음식명이 필요합니다.");
        }
        if (command.clientRequestId() == null || command.clientRequestId().isBlank()) {
            throw new InvalidTrainingSampleException("CLIENT_REQUEST_ID_REQUIRED", "요청 식별자가 필요합니다.");
        }
    }
}
