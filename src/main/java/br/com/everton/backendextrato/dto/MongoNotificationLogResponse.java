package br.com.everton.backendextrato.dto;

import java.time.Instant;

public record MongoNotificationLogResponse(
        String id,
        String userEmail,
        String title,
        String body,
        String url,
        int totalTargets,
        int totalDelivered,
        int totalRemoved,
        int totalFailed,
        int webTargets,
        int webDelivered,
        int webRemoved,
        int webFailed,
        int mobileTargets,
        int mobileDelivered,
        int mobileRemoved,
        int mobileFailed,
        Instant createdAt
) {
}
