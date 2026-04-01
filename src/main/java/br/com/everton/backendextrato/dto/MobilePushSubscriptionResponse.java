package br.com.everton.backendextrato.dto;

import java.time.OffsetDateTime;

public record MobilePushSubscriptionResponse(
        Long id,
        String expoPushToken,
        String userEmail,
        String userName,
        String platform,
        String deviceName,
        String appVersion,
        boolean created,
        OffsetDateTime updatedAt
) {
}
