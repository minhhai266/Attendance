package com.attendenceSystem.module.faceid.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FaceAiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${face-ai.base-url:http://127.0.0.1:8002}")
    private String baseUrl;

    @Value("${face-ai.api-key}")
    private String apiKey;

    public static class EmbedResult {
        public int index;
        public boolean success;
        public String embedding;
        public String message;
    }

    public static class EmbedBatchResponse {
        public List<EmbedResult> results;
    }

    public EmbedBatchResponse embedBatch(List<String> imagesBase64) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("images_base64", imagesBase64);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<EmbedBatchResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/internal/face/embed-batch",
                entity,
                EmbedBatchResponse.class
        );
        return response.getBody();
        
    }
    public static class PoseResult {
    public boolean ok;
    public String message;
    public int face_count;
    public String pose;
    public Double yaw;
    public Double pitch;
    public List<Integer> bbox;
    public Double face_ratio;
}

public PoseResult checkPose(String imageBase64) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-API-Key", apiKey);

    Map<String, Object> body = new HashMap<>();
    body.put("image_base64", imageBase64);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

    ResponseEntity<PoseResult> response = restTemplate.postForEntity(
            baseUrl + "/api/internal/face/pose-check",
            entity,
            PoseResult.class
    );
    return response.getBody();
}
}
