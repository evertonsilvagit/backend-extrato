package br.com.everton.backendextrato.application.notification.usecase.command;

public record RegisterWebSubscriptionCommand(
        String userEmail,
        String userName,
        String endpoint,
        String p256dh,
        String auth
) {}
