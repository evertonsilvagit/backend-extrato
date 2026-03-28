package br.com.everton.backendextrato.dto;

public record AuthResponse(
        String token,
        String email,
        String name,
        String photo
) {
}
