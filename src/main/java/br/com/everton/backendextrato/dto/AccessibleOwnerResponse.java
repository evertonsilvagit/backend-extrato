package br.com.everton.backendextrato.dto;

public record AccessibleOwnerResponse(
        String email,
        String name,
        String photo,
        boolean own
) {
}
