/*
 * 파일명: FeederCompatibilityController.java
 * 설명: 기존 Feeder의 multipart 계약을 내부 수집 유스케이스에 연결한다.
 * 주요기능: /api/v1/feedFoodModel 호환 요청 수신과 201 응답
 * 주의사항: URI, Method, Part 이름과 응답 상태를 임의 변경하지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthpiard.lfmserver.domain.FoodMetadata;
import com.healthpiard.lfmserver.domain.TrainingSampleCommand;
import com.healthpiard.lfmserver.domain.TrainingSampleReceipt;
import com.healthpiard.lfmserver.service.InvalidTrainingSampleException;
import com.healthpiard.lfmserver.service.TrainingSampleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class FeederCompatibilityController {
    private final TrainingSampleService trainingSampleService;
    private final ObjectMapper objectMapper;

    public FeederCompatibilityController(TrainingSampleService trainingSampleService, ObjectMapper objectMapper) {
        this.trainingSampleService = trainingSampleService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/feedFoodModel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> registerTrainingSample(
            @RequestPart("picture") MultipartFile picture,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("clientRequestId") String clientRequestId,
            @RequestPart(value = "deviceId", required = false) String deviceId,
            @RequestPart(value = "capturedAt", required = false) String capturedAt,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String requestId = resolveRequestId(clientRequestId, idempotencyKey);
        TrainingSampleReceipt receipt = trainingSampleService.registerSample(new TrainingSampleCommand(
                readImageBytes(picture),
                picture.getOriginalFilename(),
                picture.getContentType(),
                readFoodMetadata(metadataJson),
                requestId,
                deviceId,
                parseCapturedAt(capturedAt)));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", receipt));
    }

    private byte[] readImageBytes(MultipartFile picture) {
        try {
            return picture.getBytes();
        } catch (IOException exception) {
            throw new InvalidTrainingSampleException("IMAGE_READ_FAILED", "이미지 파일을 읽을 수 없습니다.");
        }
    }

    private FoodMetadata readFoodMetadata(String metadataJson) {
        try {
            JsonNode metadata = objectMapper.readTree(metadataJson);
            JsonNode foodName = metadata.get("foodName");
            return new FoodMetadata(foodName == null ? null : foodName.asText(), metadata);
        } catch (JsonProcessingException exception) {
            throw new InvalidTrainingSampleException("INVALID_METADATA", "음식 메타데이터 JSON 형식이 올바르지 않습니다.");
        }
    }

    private Instant parseCapturedAt(String capturedAt) {
        if (capturedAt == null || capturedAt.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(capturedAt);
        } catch (DateTimeParseException exception) {
            throw new InvalidTrainingSampleException("INVALID_CAPTURED_AT", "촬영시각은 ISO-8601 UTC 형식이어야 합니다.");
        }
    }

    private String resolveRequestId(String clientRequestId, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()
                && !idempotencyKey.equals(clientRequestId)) {
            throw new InvalidTrainingSampleException(
                    "IDEMPOTENCY_KEY_MISMATCH", "요청 식별자와 Idempotency-Key가 일치하지 않습니다.");
        }
        return clientRequestId;
    }
}
