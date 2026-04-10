package br.com.everton.backendextrato.application.notification.usecase.command;

public record RegisterMobileSubscriptionCommand(
        String userEmail,
        String userName,
        String expoPushToken,
        String platform,
        String deviceName,
        String appVersion
) {}
