package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.ListMobileSubscriptionsUseCase;
import br.com.everton.backendextrato.application.notification.port.out.MobilePushSubscriptionRepository;
import br.com.everton.backendextrato.domain.notification.MobileSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListMobileSubscriptionsService implements ListMobileSubscriptionsUseCase {

    private final MobilePushSubscriptionRepository mobilePushSubscriptionRepository;

    public ListMobileSubscriptionsService(MobilePushSubscriptionRepository mobilePushSubscriptionRepository) {
        this.mobilePushSubscriptionRepository = mobilePushSubscriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobileSubscription> execute(String userEmail) {
        return mobilePushSubscriptionRepository.findAllByUserEmail(userEmail.trim());
    }
}
