package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.RemoveWebSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.out.WebPushSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveWebSubscriptionService extends NotificationValidationSupport implements RemoveWebSubscriptionUseCase {

    private final WebPushSubscriptionRepository webPushSubscriptionRepository;

    public RemoveWebSubscriptionService(WebPushSubscriptionRepository webPushSubscriptionRepository) {
        this.webPushSubscriptionRepository = webPushSubscriptionRepository;
    }

    @Override
    @Transactional
    public void execute(String endpoint) {
        String normalizedEndpoint = requireText(endpoint, "endpoint e obrigatorio");
        webPushSubscriptionRepository.findByEndpoint(normalizedEndpoint).ifPresent(webPushSubscriptionRepository::delete);
    }
}
