package br.com.everton.backendextrato.application.notification.usecase.command;

public record SendNotificationTestCommand(
        String userEmail,
        String title,
        String body,
        String url
) {}
