package br.com.everton.backendextrato.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Provider;
import java.security.Security;

@Configuration
@EnableConfigurationProperties(PushVapidProperties.class)
public class PushNotificationConfig {

    @Bean
    public Provider bouncyCastleProvider() {
        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider != null) {
            return provider;
        }

        Provider created = new BouncyCastleProvider();
        Security.addProvider(created);
        return created;
    }
}
