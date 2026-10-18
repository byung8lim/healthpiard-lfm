package com.healthpiard.lfmserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthpiard.lfmserver.domain.TrainingSampleReceipt;
import com.healthpiard.lfmserver.service.TrainingSampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeederCompatibilityController.class)
class FeederCompatibilityControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TrainingSampleService trainingSampleService;

    @Test
    void acceptsExistingFeederMultipartContract() throws Exception {
        when(trainingSampleService.registerSample(any())).thenReturn(
                new TrainingSampleReceipt("request-1", "abc", false, "ACCEPTED"));
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "food.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile metadata = new MockMultipartFile(
                "metadata", "", "application/json",
                "{\"foodName\":\"김치찌개\"}".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile clientRequestId = textPart("clientRequestId", "request-1");
        MockMultipartFile deviceId = textPart("deviceId", "device-1");
        MockMultipartFile capturedAt = textPart("capturedAt", "2026-10-19T01:02:03Z");

        mockMvc.perform(multipart("/api/v1/feedFoodModel")
                        .file(picture)
                        .file(metadata)
                        .file(clientRequestId)
                        .file(deviceId)
                        .file(capturedAt)
                        .header("Idempotency-Key", "request-1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.requestId").value("request-1"));
    }

    private MockMultipartFile textPart(String name, String value) {
        return new MockMultipartFile(name, "", "text/plain", value.getBytes(StandardCharsets.UTF_8));
    }
}
