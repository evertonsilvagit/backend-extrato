package br.com.everton.backendextrato.application.auth.port.in;

import br.com.everton.backendextrato.domain.auth.UserProfile;

public interface GetProfileUseCase {
    UserProfile execute(String email);
}
