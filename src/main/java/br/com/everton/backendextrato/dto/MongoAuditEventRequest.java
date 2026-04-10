package br.com.everton.backendextrato.dto;

import java.util.Map;

public record MongoAuditEventRequest(
        String action,
        String resource,
        String source,
        Map<String, Object> payload
) {
}
