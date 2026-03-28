package br.com.everton.backendextrato.dto;

public record PushSubscriptionRequest(
        String endpoint,
        String p256dh,
        String auth,
        String userEmail,
        String userName
) {
}
