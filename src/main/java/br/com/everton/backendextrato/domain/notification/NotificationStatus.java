package br.com.everton.backendextrato.domain.notification;

public record NotificationStatus(
        boolean configured,
        boolean valid,
        String publicKey
) {}
