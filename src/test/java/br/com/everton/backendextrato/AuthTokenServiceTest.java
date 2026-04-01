package br.com.everton.backendextrato;

import br.com.everton.backendextrato.auth.AuthTokenService;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.config.AuthProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthTokenServiceTest {

    @Test
    void shouldGenerateAndParseTokenEvenWithShortSecret() {
        AuthTokenService service = new AuthTokenService(new AuthProperties("short-secret", "backend-extrato", 86400));

        String token = service.generateToken(new AuthenticatedUser(1L, "user@example.com", "User"));

        assertNotNull(token);
        AuthenticatedUser parsed = service.parseToken(token);
        assertEquals(1L, parsed.id());
        assertEquals("user@example.com", parsed.email());
        assertEquals("User", parsed.name());
    }
}
