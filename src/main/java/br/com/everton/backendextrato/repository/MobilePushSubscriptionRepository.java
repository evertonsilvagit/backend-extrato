package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.MobilePushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MobilePushSubscriptionRepository extends JpaRepository<MobilePushSubscription, Long> {
    Optional<MobilePushSubscription> findByExpoPushToken(String expoPushToken);
    List<MobilePushSubscription> findAllByUserEmailIgnoreCase(String userEmail);
}
