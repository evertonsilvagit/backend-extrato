package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.ListWebSubscriptionsUseCase;
import br.com.everton.backendextrato.application.notification.port.out.WebPushSubscriptionRepository;
import br.com.everton.backendextrato.domain.notification.WebPushSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListWebSubscriptionsService implements ListWebSubscriptionsUseCase {

    private final WebPushSubscriptionRepository webPushSubscriptionRepository;

    public ListWebSubscriptionsService(WebPushSubscriptionRepository webPushSubscriptionRepository) {
        this.webPushSubscriptionRepository = webPushSubscriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebPushSubscription> execute(String userEmail) {
        return webPushSubscriptionRepository.findAllByUserEmail(userEmail.trim());
    }
}
