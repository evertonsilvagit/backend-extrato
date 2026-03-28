package br.com.everton.backendextrato.dto;

public record PushNotificationStatusResponse(
        boolean vapidConfigured,
        boolean vapidKeyPairValid,
        String publicKey
) {
}
