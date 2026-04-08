package br.com.everton.backendextrato.dto;

import java.util.Map;

public record MongoLearningEventRequest(
        String title,
        String type,
        Map<String, Object> payload
) {
}
