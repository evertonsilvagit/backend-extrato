package br.com.everton.backendextrato.auth;

import br.com.everton.backendextrato.config.AuthProperties;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

@Service
public class AuthTokenService {

    private final AuthProperties authProperties;
    private final HmacKey hmacKey;

    public AuthTokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.hmacKey = new HmacKey(deriveSigningKey(authProperties.secret()));
    }

    public String generateToken(AuthenticatedUser user) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(authProperties.issuer());
            claims.setSubject(user.email());
            claims.setClaim("userId", user.id());
            claims.setClaim("name", user.name());
            claims.setExpirationTimeMinutesInTheFuture(authProperties.expirationSeconds() / 60f);
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(1);

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(hmacKey);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            return jws.getCompactSerialization();
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar token de autenticação.", ex);
        }
    }

    public AuthenticatedUser parseToken(String token) {
        try {
            JwtConsumer consumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setExpectedIssuer(authProperties.issuer())
                    .setVerificationKey(hmacKey)
                    .build();

            JwtClaims claims = consumer.processToClaims(token);
            Long userId = claims.getClaimValue("userId", Long.class);
            String email = claims.getSubject();
            String name = claims.getStringClaimValue("name");
            return new AuthenticatedUser(userId, email, name);
        } catch (InvalidJwtException ex) {
            throw new IllegalArgumentException("Token inválido.", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao validar token.", ex);
        }
    }

    private byte[] deriveSigningKey(String secret) {
        String normalizedSecret = secret == null || secret.isBlank()
                ? "backend-extrato-local-dev-secret-change-me"
                : secret;

        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(normalizedSecret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Falha ao preparar a chave de autenticação.", ex);
        }
    }
}
