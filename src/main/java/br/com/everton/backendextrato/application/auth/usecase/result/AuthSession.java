package br.com.everton.backendextrato.application.auth.usecase.result;

public record AuthSession(
        String token,
        String email,
        String name,
        String photo
) {}
