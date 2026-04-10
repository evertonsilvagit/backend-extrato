package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.RegisterWebSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.out.WebPushSubscriptionRepository;
import br.com.everton.backendextrato.application.notification.usecase.command.RegisterWebSubscriptionCommand;
import br.com.everton.backendextrato.application.notification.usecase.result.WebSubscriptionRegistrationResult;
import br.com.everton.backendextrato.domain.notification.WebPushSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterWebSubscriptionService extends NotificationValidationSupport implements RegisterWebSubscriptionUseCase {

    private final WebPushSubscriptionRepository webPushSubscriptionRepository;

    public RegisterWebSubscriptionService(WebPushSubscriptionRepository webPushSubscriptionRepository) {
        this.webPushSubscriptionRepository = webPushSubscriptionRepository;
    }

    @Override
    @Transactional
    public WebSubscriptionRegistrationResult execute(RegisterWebSubscriptionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }

        String endpoint = requireText(command.endpoint(), "endpoint e obrigatorio");
        WebPushSubscription existing = webPushSubscriptionRepository.findByEndpoint(endpoint).orElse(null);
        boolean created = existing == null;

        WebPushSubscription saved = webPushSubscriptionRepository.save(new WebPushSubscription(
                existing == null ? null : existing.id(),
                endpoint,
                requireText(command.p256dh(), "p256dh e obrigatorio"),
                requireText(command.auth(), "auth e obrigatorio"),
                normalize(command.userEmail()),
                normalize(command.userName()),
                existing == null ? null : existing.createdAt(),
                existing == null ? null : existing.updatedAt()
        ));

        return new WebSubscriptionRegistrationResult(saved, created);
    }
}
