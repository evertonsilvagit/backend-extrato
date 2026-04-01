package br.com.everton.backendextrato.dto;

import java.time.OffsetDateTime;

public record MobilePushSubscriptionDetailsResponse(
        Long id,
        String expoPushToken,
        String userEmail,
        String userName,
        String platform,
        String deviceName,
        String appVersion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
