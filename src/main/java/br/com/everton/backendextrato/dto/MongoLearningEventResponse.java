package br.com.everton.backendextrato.dto;

import java.time.Instant;
import java.util.Map;

public record MongoLearningEventResponse(
        String id,
        String userEmail,
        String title,
        String type,
        Map<String, Object> payload,
        Instant createdAt
) {
}
