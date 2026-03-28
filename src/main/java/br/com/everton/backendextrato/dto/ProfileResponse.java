package br.com.everton.backendextrato.dto;

public record ProfileResponse(
        String email,
        String name,
        String photo
) {
}
