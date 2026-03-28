package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.PushSubscriptionDetailsResponse;
import br.com.everton.backendextrato.dto.PushSubscriptionRequest;
import br.com.everton.backendextrato.dto.PushSubscriptionResponse;
import br.com.everton.backendextrato.model.PushSubscription;
import br.com.everton.backendextrato.repository.PushSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PushSubscriptionService {

    private final PushSubscriptionRepository repository;

    public PushSubscriptionService(PushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PushSubscriptionResponse register(String userEmail, String userName, PushSubscriptionRequest request) {
        validate(request);

        PushSubscription subscription = repository.findByEndpoint(request.endpoint())
                .orElseGet(PushSubscription::new);
        boolean created = subscription.getId() == null;

        subscription.setEndpoint(request.endpoint().trim());
        subscription.setP256dh(request.p256dh().trim());
        subscription.setAuth(request.auth().trim());
        subscription.setUserEmail(normalize(userEmail));
        subscription.setUserName(normalize(userName));

        PushSubscription saved = repository.save(subscription);
        return new PushSubscriptionResponse(
                saved.getId(),
                saved.getEndpoint(),
                saved.getUserEmail(),
                saved.getUserName(),
                created
        );
    }

    @Transactional
    public void removeByEndpoint(String endpoint) {
        String normalizedEndpoint = requireText(endpoint, "endpoint e obrigatorio");
        repository.findByEndpoint(normalizedEndpoint).ifPresent(repository::delete);
    }

    @Transactional(readOnly = true)
    public List<PushSubscriptionDetailsResponse> list(String userEmail) {
        List<PushSubscription> subscriptions =
                repository.findAllByUserEmailIgnoreCase(userEmail.trim());

        return subscriptions.stream()
                .map(subscription -> new PushSubscriptionDetailsResponse(
                        subscription.getId(),
                        subscription.getEndpoint(),
                        subscription.getUserEmail(),
                        subscription.getUserName(),
                        subscription.getCreatedAt(),
                        subscription.getUpdatedAt()
                ))
                .toList();
    }

    private void validate(PushSubscriptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        requireText(request.endpoint(), "endpoint e obrigatorio");
        requireText(request.p256dh(), "p256dh e obrigatorio");
        requireText(request.auth(), "auth e obrigatorio");
    }

    private String requireText(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
