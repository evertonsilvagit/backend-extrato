package br.com.everton.backendextrato.domain.auth;

public record UserProfile(
        Long id,
        String email,
        String displayName,
        String profileImageUrl,
        String passwordHash
) {}
