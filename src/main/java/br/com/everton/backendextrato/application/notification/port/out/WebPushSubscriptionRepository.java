package br.com.everton.backendextrato.application.notification.port.out;

import br.com.everton.backendextrato.domain.notification.WebPushSubscription;

import java.util.List;
import java.util.Optional;

public interface WebPushSubscriptionRepository {
    Optional<WebPushSubscription> findByEndpoint(String endpoint);
    WebPushSubscription save(WebPushSubscription subscription);
    void delete(WebPushSubscription subscription);
    List<WebPushSubscription> findAllByUserEmail(String userEmail);
}
