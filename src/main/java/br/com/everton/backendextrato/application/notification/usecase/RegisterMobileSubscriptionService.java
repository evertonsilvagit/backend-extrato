package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.RegisterMobileSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.out.MobilePushSubscriptionRepository;
import br.com.everton.backendextrato.application.notification.usecase.command.RegisterMobileSubscriptionCommand;
import br.com.everton.backendextrato.application.notification.usecase.result.MobileSubscriptionRegistrationResult;
import br.com.everton.backendextrato.domain.notification.MobileSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterMobileSubscriptionService extends NotificationValidationSupport implements RegisterMobileSubscriptionUseCase {

    private final MobilePushSubscriptionRepository mobilePushSubscriptionRepository;

    public RegisterMobileSubscriptionService(MobilePushSubscriptionRepository mobilePushSubscriptionRepository) {
        this.mobilePushSubscriptionRepository = mobilePushSubscriptionRepository;
    }

    @Override
    @Transactional
    public MobileSubscriptionRegistrationResult execute(RegisterMobileSubscriptionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }

        String expoPushToken = requireText(command.expoPushToken(), "expoPushToken e obrigatorio");
        MobileSubscription existing = mobilePushSubscriptionRepository.findByExpoPushToken(expoPushToken).orElse(null);
        boolean created = existing == null;

        MobileSubscription saved = mobilePushSubscriptionRepository.save(new MobileSubscription(
                existing == null ? null : existing.id(),
                expoPushToken,
                normalize(command.userEmail()),
                normalize(command.userName()),
                requireText(command.platform(), "platform e obrigatorio").toLowerCase(),
                normalize(command.deviceName()),
                normalize(command.appVersion()),
                existing == null ? null : existing.createdAt(),
                existing == null ? null : existing.updatedAt()
        ));

        return new MobileSubscriptionRegistrationResult(saved, created);
    }
}
