package br.com.everton.backendextrato.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public record AuthProperties(
        String secret,
        String issuer,
        long expirationSeconds
) {
}
