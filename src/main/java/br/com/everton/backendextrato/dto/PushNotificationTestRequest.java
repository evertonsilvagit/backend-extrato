package br.com.everton.backendextrato.dto;

public record PushNotificationTestRequest(
        String title,
        String body,
        String url,
        String userEmail
) {
}
