package br.com.everton.backendextrato.application.auth.usecase.command;

public record UpdateProfileCommand(
        String name,
        String photo
) {}
