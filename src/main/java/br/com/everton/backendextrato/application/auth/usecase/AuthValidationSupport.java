package br.com.everton.backendextrato.application.auth.usecase;

import br.com.everton.backendextrato.application.auth.usecase.result.AuthSession;
import br.com.everton.backendextrato.domain.auth.UserProfile;

abstract class AuthValidationSupport {

    protected AuthSession toSession(UserProfile userProfile, String token) {
        return new AuthSession(token, userProfile.email(), userProfile.displayName(), userProfile.profileImageUrl());
    }

    protected String requireEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório.");
        }
        return email.trim().toLowerCase();
    }

    protected String requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }
        return password;
    }

    protected String requireDisplayName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres.");
        }
        if (trimmedName.length() > 120) {
            throw new IllegalArgumentException("Nome deve ter no máximo 120 caracteres.");
        }
        return trimmedName;
    }

    protected String normalizePhoto(String photo) {
        if (photo == null || photo.isBlank()) {
            return null;
        }

        String trimmedPhoto = photo.trim();
        if (!trimmedPhoto.startsWith("data:image/")) {
            throw new IllegalArgumentException("Foto inválida. Envie uma imagem válida.");
        }
        if (trimmedPhoto.length() > 2_000_000) {
            throw new IllegalArgumentException("Foto muito grande. Escolha uma imagem menor.");
        }
        return trimmedPhoto;
    }
}
