package br.com.everton.backendextrato.application.auth.usecase.command;

public record AuthCommand(
        String email,
        String password,
        String name,
        String photo
) {}
