package br.com.everton.backendextrato.infrastructure.auth;

import br.com.everton.backendextrato.application.auth.port.out.AuthTokenPort;
import br.com.everton.backendextrato.auth.AuthTokenService;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.domain.auth.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenAdapter implements AuthTokenPort {

    private final AuthTokenService authTokenService;

    public AuthTokenAdapter(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public String issue(UserProfile userProfile) {
        return authTokenService.generateToken(
                new AuthenticatedUser(userProfile.id(), userProfile.email(), userProfile.displayName())
        );
    }
}
