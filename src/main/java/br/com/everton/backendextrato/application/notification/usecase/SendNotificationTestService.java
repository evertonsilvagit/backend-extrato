package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.SendNotificationTestUseCase;
import br.com.everton.backendextrato.application.notification.port.out.NotificationDeliveryPort;
import br.com.everton.backendextrato.application.notification.usecase.command.SendNotificationTestCommand;
import br.com.everton.backendextrato.domain.notification.NotificationDeliveryResult;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationTestService extends NotificationValidationSupport implements SendNotificationTestUseCase {

    private final NotificationDeliveryPort notificationDeliveryPort;

    public SendNotificationTestService(NotificationDeliveryPort notificationDeliveryPort) {
        this.notificationDeliveryPort = notificationDeliveryPort;
    }

    @Override
    public NotificationDeliveryResult execute(SendNotificationTestCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        validatePayload(command.title(), command.body());
        return notificationDeliveryPort.sendWeb(command.userEmail(), command.title(), command.body(), command.url())
                .plus(notificationDeliveryPort.sendMobile(command.userEmail(), command.title(), command.body(), command.url()));
    }

    private void validatePayload(String title, String body) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title e obrigatorio");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body e obrigatorio");
        }
    }
}
