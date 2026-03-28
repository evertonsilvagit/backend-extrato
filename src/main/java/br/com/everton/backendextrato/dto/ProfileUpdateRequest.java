package br.com.everton.backendextrato.dto;

public record ProfileUpdateRequest(
        String name,
        String photo
) {
}
