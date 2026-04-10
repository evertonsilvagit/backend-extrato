package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.RemoveMobileSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.out.MobilePushSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveMobileSubscriptionService extends NotificationValidationSupport implements RemoveMobileSubscriptionUseCase {

    private final MobilePushSubscriptionRepository mobilePushSubscriptionRepository;

    public RemoveMobileSubscriptionService(MobilePushSubscriptionRepository mobilePushSubscriptionRepository) {
        this.mobilePushSubscriptionRepository = mobilePushSubscriptionRepository;
    }

    @Override
    @Transactional
    public void execute(String expoPushToken) {
        String normalizedToken = requireText(expoPushToken, "expoPushToken e obrigatorio");
        mobilePushSubscriptionRepository.findByExpoPushToken(normalizedToken).ifPresent(mobilePushSubscriptionRepository::delete);
    }
}
