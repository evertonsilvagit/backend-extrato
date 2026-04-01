package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.MobilePushSubscriptionDetailsResponse;
import br.com.everton.backendextrato.dto.MobilePushSubscriptionRequest;
import br.com.everton.backendextrato.dto.MobilePushSubscriptionResponse;
import br.com.everton.backendextrato.model.MobilePushSubscription;
import br.com.everton.backendextrato.repository.MobilePushSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MobilePushSubscriptionService {

    private final MobilePushSubscriptionRepository repository;

    public MobilePushSubscriptionService(MobilePushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MobilePushSubscriptionResponse register(String userEmail, String userName, MobilePushSubscriptionRequest request) {
        validate(request);

        MobilePushSubscription subscription = repository.findByExpoPushToken(request.expoPushToken().trim())
                .orElseGet(MobilePushSubscription::new);
        boolean created = subscription.getId() == null;

        subscription.setExpoPushToken(request.expoPushToken().trim());
        subscription.setPlatform(requireText(request.platform(), "platform e obrigatorio").toLowerCase());
        subscription.setUserEmail(normalize(userEmail));
        subscription.setUserName(normalize(userName));
        subscription.setDeviceName(normalize(request.deviceName()));
        subscription.setAppVersion(normalize(request.appVersion()));

        MobilePushSubscription saved = repository.save(subscription);
        return new MobilePushSubscriptionResponse(
                saved.getId(),
                saved.getExpoPushToken(),
                saved.getUserEmail(),
                saved.getUserName(),
                saved.getPlatform(),
                saved.getDeviceName(),
                saved.getAppVersion(),
                created,
                saved.getUpdatedAt()
        );
    }

    @Transactional
    public void removeByExpoPushToken(String expoPushToken) {
        String normalizedToken = requireText(expoPushToken, "expoPushToken e obrigatorio");
        repository.findByExpoPushToken(normalizedToken).ifPresent(repository::delete);
    }

    @Transactional(readOnly = true)
    public List<MobilePushSubscriptionDetailsResponse> list(String userEmail) {
        return repository.findAllByUserEmailIgnoreCase(userEmail.trim()).stream()
                .map(subscription -> new MobilePushSubscriptionDetailsResponse(
                        subscription.getId(),
                        subscription.getExpoPushToken(),
                        subscription.getUserEmail(),
                        subscription.getUserName(),
                        subscription.getPlatform(),
                        subscription.getDeviceName(),
                        subscription.getAppVersion(),
                        subscription.getCreatedAt(),
                        subscription.getUpdatedAt()
                ))
                .toList();
    }

    private void validate(MobilePushSubscriptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        requireText(request.expoPushToken(), "expoPushToken e obrigatorio");
        requireText(request.platform(), "platform e obrigatorio");
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
