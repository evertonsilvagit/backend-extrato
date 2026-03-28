package br.com.everton.backendextrato.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserResolver {

    public AuthenticatedUser require(HttpServletRequest request) {
        Object value = request.getAttribute(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE);
        if (value instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }
        throw new IllegalStateException("Usuário autenticado não encontrado na requisição.");
    }
}
