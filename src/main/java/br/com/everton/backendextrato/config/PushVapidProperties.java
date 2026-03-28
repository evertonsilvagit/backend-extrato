package br.com.everton.backendextrato.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "push.vapid")
public record PushVapidProperties(
        String subject,
        String publicKey,
        String privateKey
) {
    public boolean isConfigured() {
        return hasText(subject) && hasText(publicKey) && hasText(privateKey);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
