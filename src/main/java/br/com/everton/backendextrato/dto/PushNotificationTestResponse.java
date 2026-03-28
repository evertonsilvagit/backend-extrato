package br.com.everton.backendextrato.dto;

public record PushNotificationTestResponse(
        int targetCount,
        int deliveredCount,
        int removedCount,
        int failedCount
) {
}
