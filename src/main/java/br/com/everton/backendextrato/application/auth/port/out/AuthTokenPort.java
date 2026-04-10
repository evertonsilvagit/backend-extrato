package br.com.everton.backendextrato.application.auth.port.out;

import br.com.everton.backendextrato.domain.auth.UserProfile;

public interface AuthTokenPort {
    String issue(UserProfile userProfile);
}
