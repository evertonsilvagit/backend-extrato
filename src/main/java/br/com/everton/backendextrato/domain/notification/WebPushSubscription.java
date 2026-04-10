package br.com.everton.backendextrato.domain.notification;

import java.time.OffsetDateTime;

public record WebPushSubscription(
        Long id,
        String endpoint,
        String p256dh,
        String auth,
        String userEmail,
        String userName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
