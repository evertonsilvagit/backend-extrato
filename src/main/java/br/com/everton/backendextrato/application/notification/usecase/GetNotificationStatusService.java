package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.GetNotificationStatusUseCase;
import br.com.everton.backendextrato.application.notification.port.out.NotificationDeliveryPort;
import br.com.everton.backendextrato.domain.notification.NotificationStatus;
import org.springframework.stereotype.Service;

@Service
public class GetNotificationStatusService implements GetNotificationStatusUseCase {

    private final NotificationDeliveryPort notificationDeliveryPort;

    public GetNotificationStatusService(NotificationDeliveryPort notificationDeliveryPort) {
        this.notificationDeliveryPort = notificationDeliveryPort;
    }

    @Override
    public NotificationStatus execute() {
        return notificationDeliveryPort.getStatus();
    }
}
