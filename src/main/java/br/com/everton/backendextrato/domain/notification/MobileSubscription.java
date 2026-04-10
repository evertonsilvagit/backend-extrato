package br.com.everton.backendextrato.domain.notification;

import java.time.OffsetDateTime;

public record MobileSubscription(
        Long id,
        String expoPushToken,
        String userEmail,
        String userName,
        String platform,
        String deviceName,
        String appVersion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
