package br.com.everton.backendextrato.dto;

import java.time.Instant;
import java.util.Map;

public record MongoImportHistoryResponse(
        String id,
        String userEmail,
        String importType,
        String source,
        int total,
        int created,
        int updated,
        int failed,
        long durationMs,
        Map<String, Object> metadata,
        Instant createdAt
) {
}
