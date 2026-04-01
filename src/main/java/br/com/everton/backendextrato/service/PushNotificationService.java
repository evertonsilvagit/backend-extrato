package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.config.PushVapidProperties;
import br.com.everton.backendextrato.dto.PushNotificationTestRequest;
import br.com.everton.backendextrato.dto.PushNotificationStatusResponse;
import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.model.PushSubscription;
import br.com.everton.backendextrato.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushSubscriptionRepository repository;
    private final PushVapidProperties vapidProperties;

    public PushNotificationService(
            PushSubscriptionRepository repository,
            PushVapidProperties vapidProperties
    ) {
        this.repository = repository;
        this.vapidProperties = vapidProperties;
    }

    public PushNotificationStatusResponse getStatus() {
        boolean configured = vapidProperties.isConfigured();
        return new PushNotificationStatusResponse(
                configured,
                configured && isVapidKeyPairValid(),
                hasText(vapidProperties.publicKey()) ? vapidProperties.publicKey() : null
        );
    }

    @Transactional
    public PushNotificationTestResponse sendTest(String userEmail, PushNotificationTestRequest request) {
        validate(request);
        return sendToUser(userEmail, request.title(), request.body(), request.url());
    }

    @Transactional
    public PushNotificationTestResponse sendToUser(String userEmail, String title, String body, String url) {
        validatePayload(title, body);

        List<PushSubscription> targets = findTargets(userEmail);
        if (targets.isEmpty()) {
            log.info("Push requested but no subscriptions matched userEmail={}", sanitize(userEmail));
            return new PushNotificationTestResponse(0, 0, 0, 0);
        }

        ensureConfigured();

        String payload = buildPayload(title, body, url);
        PushService pushService = buildPushService();

        log.info(
                "Sending push to {} subscription(s) with userEmail={} title={}",
                targets.size(),
                sanitize(userEmail),
                sanitize(title)
        );

        int delivered = 0;
        int removed = 0;
        int failed = 0;

        for (PushSubscription subscription : targets) {
            try {
                HttpResponse response = pushService.send(
                        new Notification(
                                subscription.getEndpoint(),
                                subscription.getP256dh(),
                                subscription.getAuth(),
                                payload.getBytes(StandardCharsets.UTF_8)
                        ),
                        Encoding.AES128GCM
                );

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    log.info(
                            "Push delivered with status={} subscriptionId={} endpoint={} userEmail={} createdAt={} updatedAt={}",
                            statusCode,
                            subscription.getId(),
                            abbreviateEndpoint(subscription.getEndpoint()),
                            sanitize(subscription.getUserEmail()),
                            subscription.getCreatedAt(),
                            subscription.getUpdatedAt()
                    );
                    delivered++;
                } else {
                    log.warn(
                            "Push delivery failed with status={} reason={} subscriptionId={} endpoint={} userEmail={} createdAt={} updatedAt={}",
                            statusCode,
                            sanitize(response.getStatusLine().getReasonPhrase()),
                            subscription.getId(),
                            abbreviateEndpoint(subscription.getEndpoint()),
                            sanitize(subscription.getUserEmail()),
                            subscription.getCreatedAt(),
                            subscription.getUpdatedAt()
                    );
                    failed++;
                }
            } catch (Exception ex) {
                log.warn(
                        "Push delivery raised exception type={} message={} subscriptionId={} endpoint={} userEmail={} createdAt={} updatedAt={}",
                        ex.getClass().getSimpleName(),
                        sanitize(ex.getMessage()),
                        subscription.getId(),
                        abbreviateEndpoint(subscription.getEndpoint()),
                        sanitize(subscription.getUserEmail()),
                        subscription.getCreatedAt(),
                        subscription.getUpdatedAt()
                );
                failed++;
            }
        }

        return new PushNotificationTestResponse(targets.size(), delivered, removed, failed);
    }

    private List<PushSubscription> findTargets(String userEmail) {
        return repository.findAllByUserEmailIgnoreCase(userEmail.trim());
    }

    private void validate(PushNotificationTestRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        validatePayload(request.title(), request.body());
    }

    private void validatePayload(String title, String body) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title e obrigatorio");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body e obrigatorio");
        }
    }

    private void ensureConfigured() {
        if (!vapidProperties.isConfigured()) {
            log.error("Push send rejected because VAPID configuration is incomplete");
            throw new IllegalStateException("Push VAPID nao configurado. Defina push.vapid.subject, push.vapid.public-key e push.vapid.private-key.");
        }
        if (!isVapidKeyPairValid()) {
            log.error("Push send rejected because configured VAPID key pair is invalid");
            throw new IllegalStateException("As chaves VAPID configuradas nao formam um par valido. Atualize PUSH_VAPID_PUBLIC_KEY e PUSH_VAPID_PRIVATE_KEY.");
        }
    }

    private PushService buildPushService() {
        try {
            log.debug("Initializing Web Push service with subject={}", sanitize(vapidProperties.subject()));
            return new PushService(vapidProperties.publicKey(), vapidProperties.privateKey(), vapidProperties.subject());
        } catch (GeneralSecurityException ex) {
            log.error("Failed to initialize Web Push service: {}", sanitize(ex.getMessage()));
            throw new IllegalStateException("Falha ao inicializar Web Push com as chaves VAPID configuradas.", ex);
        }
    }

    private String buildPayload(String title, String body, String url) {
        StringBuilder payload = new StringBuilder();
        payload.append("{");
        payload.append("\"title\":\"").append(escapeJson(title)).append("\"");
        payload.append(",\"body\":\"").append(escapeJson(body)).append("\"");
        if (url != null && !url.isBlank()) {
            payload.append(",\"url\":\"").append(escapeJson(url.trim())).append("\"");
        }
        payload.append("}");
        return payload.toString();
    }

    private String abbreviateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return "<empty>";
        }
        if (endpoint.length() <= 96) {
            return endpoint;
        }
        return endpoint.substring(0, 48) + "..." + endpoint.substring(endpoint.length() - 24);
    }

    private String sanitize(String value) {
        if (value == null) {
            return "<null>";
        }
        String sanitized = value.replace("\r", " ").replace("\n", " ").trim();
        return sanitized.isEmpty() ? "<empty>" : sanitized;
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isVapidKeyPairValid() {
        try {
            return Utils.verifyKeyPair(
                    Utils.loadPrivateKey(vapidProperties.privateKey()),
                    Utils.loadPublicKey(vapidProperties.publicKey())
            );
        } catch (GeneralSecurityException | RuntimeException ex) {
            log.warn("Unable to validate configured VAPID key pair: {}", sanitize(ex.getMessage()));
            return false;
        }
    }
}
