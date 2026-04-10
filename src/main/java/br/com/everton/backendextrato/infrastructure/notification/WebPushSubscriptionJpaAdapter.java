package br.com.everton.backendextrato.infrastructure.notification;

import br.com.everton.backendextrato.application.notification.port.out.WebPushSubscriptionRepository;
import br.com.everton.backendextrato.domain.notification.WebPushSubscription;
import br.com.everton.backendextrato.model.PushSubscription;
import org.springframework.stereotype.Component;

@Component
public class WebPushSubscriptionJpaAdapter implements WebPushSubscriptionRepository {

    private final br.com.everton.backendextrato.repository.PushSubscriptionRepository repository;

    public WebPushSubscriptionJpaAdapter(br.com.everton.backendextrato.repository.PushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public java.util.Optional<WebPushSubscription> findByEndpoint(String endpoint) {
        return repository.findByEndpoint(endpoint).map(this::toDomain);
    }

    @Override
    public WebPushSubscription save(WebPushSubscription subscription) {
        PushSubscription entity = subscription.id() != null ? repository.findById(subscription.id()).orElseGet(PushSubscription::new) : new PushSubscription();
        entity.setEndpoint(subscription.endpoint());
        entity.setP256dh(subscription.p256dh());
        entity.setAuth(subscription.auth());
        entity.setUserEmail(subscription.userEmail());
        entity.setUserName(subscription.userName());
        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(WebPushSubscription subscription) {
        repository.findById(subscription.id()).ifPresent(repository::delete);
    }

    @Override
    public java.util.List<WebPushSubscription> findAllByUserEmail(String userEmail) {
        return repository.findAllByUserEmailIgnoreCase(userEmail).stream().map(this::toDomain).toList();
    }

    private WebPushSubscription toDomain(PushSubscription entity) {
        return new WebPushSubscription(
                entity.getId(),
                entity.getEndpoint(),
                entity.getP256dh(),
                entity.getAuth(),
                entity.getUserEmail(),
                entity.getUserName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
