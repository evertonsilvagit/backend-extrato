package br.com.everton.backendextrato.dto;

public record PushSubscriptionResponse(
        Long id,
        String endpoint,
        String userEmail,
        String userName,
        boolean created
) {
}
