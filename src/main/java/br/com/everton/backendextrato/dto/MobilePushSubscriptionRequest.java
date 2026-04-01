package br.com.everton.backendextrato.dto;

public record MobilePushSubscriptionRequest(
        String expoPushToken,
        String platform,
        String deviceName,
        String appVersion
) {
}
