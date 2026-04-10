package br.com.everton.backendextrato.infrastructure.notification;

import br.com.everton.backendextrato.application.notification.port.out.MobilePushSubscriptionRepository;
import br.com.everton.backendextrato.domain.notification.MobileSubscription;
import br.com.everton.backendextrato.model.MobilePushSubscription;
import org.springframework.stereotype.Component;

@Component
public class MobilePushSubscriptionJpaAdapter implements MobilePushSubscriptionRepository {

    private final br.com.everton.backendextrato.repository.MobilePushSubscriptionRepository repository;

    public MobilePushSubscriptionJpaAdapter(br.com.everton.backendextrato.repository.MobilePushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public java.util.Optional<MobileSubscription> findByExpoPushToken(String expoPushToken) {
        return repository.findByExpoPushToken(expoPushToken).map(this::toDomain);
    }

    @Override
    public MobileSubscription save(MobileSubscription subscription) {
        MobilePushSubscription entity = subscription.id() != null ? repository.findById(subscription.id()).orElseGet(MobilePushSubscription::new) : new MobilePushSubscription();
        entity.setExpoPushToken(subscription.expoPushToken());
        entity.setUserEmail(subscription.userEmail());
        entity.setUserName(subscription.userName());
        entity.setPlatform(subscription.platform());
        entity.setDeviceName(subscription.deviceName());
        entity.setAppVersion(subscription.appVersion());
        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(MobileSubscription subscription) {
        repository.findById(subscription.id()).ifPresent(repository::delete);
    }

    @Override
    public java.util.List<MobileSubscription> findAllByUserEmail(String userEmail) {
        return repository.findAllByUserEmailIgnoreCase(userEmail).stream().map(this::toDomain).toList();
    }

    private MobileSubscription toDomain(MobilePushSubscription entity) {
        return new MobileSubscription(
                entity.getId(),
                entity.getExpoPushToken(),
                entity.getUserEmail(),
                entity.getUserName(),
                entity.getPlatform(),
                entity.getDeviceName(),
                entity.getAppVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
