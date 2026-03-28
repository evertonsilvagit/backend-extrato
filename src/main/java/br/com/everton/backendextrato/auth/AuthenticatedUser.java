package br.com.everton.backendextrato.auth;

public record AuthenticatedUser(
        Long id,
        String email,
        String name
) {
}
