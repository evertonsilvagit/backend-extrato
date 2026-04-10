package br.com.everton.backendextrato.application.notification.port.out;

import br.com.everton.backendextrato.domain.notification.MobileSubscription;

import java.util.List;
import java.util.Optional;

public interface MobilePushSubscriptionRepository {
    Optional<MobileSubscription> findByExpoPushToken(String expoPushToken);
    MobileSubscription save(MobileSubscription subscription);
    void delete(MobileSubscription subscription);
    List<MobileSubscription> findAllByUserEmail(String userEmail);
}
