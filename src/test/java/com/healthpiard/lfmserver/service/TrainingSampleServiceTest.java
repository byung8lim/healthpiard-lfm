package com.healthpiard.lfmserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthpiard.lfmserver.config.IngestProperties;
import com.healthpiard.lfmserver.domain.FoodMetadata;
import com.healthpiard.lfmserver.domain.StoredImage;
import com.healthpiard.lfmserver.domain.TrainingSampleCommand;
import com.healthpiard.lfmserver.domain.TrainingSampleReceipt;
import com.healthpiard.lfmserver.repository.ImageObjectStorage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainingSampleServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ImageObjectStorage storage = (bytes, type) -> new StoredImage("abc", "ab/abc.jpg", bytes.length, false);
    private final TrainingSampleService service = new TrainingSampleService(
            storage, new IngestProperties(Path.of("build/test-storage"), 10));

    @Test
    void registersValidSampleWithoutStartingTraining() throws Exception {
        TrainingSampleReceipt receipt = service.registerSample(command(new byte[]{1, 2, 3}, "image/jpeg", "김치찌개"));

        assertThat(receipt.status()).isEqualTo("ACCEPTED");
        assertThat(receipt.imageSha256()).isEqualTo("abc");
        assertThat(receipt.requestId()).isEqualTo("request-1");
    }

    @Test
    void rejectsUnsupportedImageType() throws Exception {
        assertThatThrownBy(() -> service.registerSample(command(new byte[]{1}, "image/gif", "김치찌개")))
                .isInstanceOf(InvalidTrainingSampleException.class)
                .extracting("code")
                .isEqualTo("UNSUPPORTED_IMAGE_TYPE");
    }

    @Test
    void rejectsOversizedImage() throws Exception {
        assertThatThrownBy(() -> service.registerSample(command(new byte[11], "image/jpeg", "김치찌개")))
                .isInstanceOf(InvalidTrainingSampleException.class)
                .extracting("code")
                .isEqualTo("IMAGE_TOO_LARGE");
    }

    private TrainingSampleCommand command(byte[] bytes, String contentType, String foodName) throws Exception {
        return new TrainingSampleCommand(bytes, "food.jpg", contentType,
                new FoodMetadata(foodName, objectMapper.readTree("{\"foodName\":\"김치찌개\"}")),
                "request-1", "device-1", null);
    }
}
