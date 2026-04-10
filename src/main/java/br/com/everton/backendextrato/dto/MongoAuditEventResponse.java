package br.com.everton.backendextrato.dto;

import java.time.Instant;
import java.util.Map;

public record MongoAuditEventResponse(
        String id,
        String userEmail,
        String action,
        String resource,
        String source,
        Map<String, Object> payload,
        Instant createdAt
) {
}
