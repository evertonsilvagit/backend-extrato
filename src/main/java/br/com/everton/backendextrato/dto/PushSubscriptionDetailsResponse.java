package br.com.everton.backendextrato.dto;

import java.time.OffsetDateTime;

public record PushSubscriptionDetailsResponse(
        Long id,
        String endpoint,
        String userEmail,
        String userName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
