package br.com.everton.backendextrato.infrastructure.notification;

import br.com.everton.backendextrato.application.notification.port.out.NotificationDeliveryPort;
import br.com.everton.backendextrato.domain.notification.NotificationDeliveryResult;
import br.com.everton.backendextrato.domain.notification.NotificationStatus;
import br.com.everton.backendextrato.dto.PushNotificationStatusResponse;
import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.service.ExpoPushNotificationService;
import br.com.everton.backendextrato.service.PushNotificationService;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryAdapter implements NotificationDeliveryPort {

    private final PushNotificationService pushNotificationService;
    private final ExpoPushNotificationService expoPushNotificationService;

    public NotificationDeliveryAdapter(
            PushNotificationService pushNotificationService,
            ExpoPushNotificationService expoPushNotificationService
    ) {
        this.pushNotificationService = pushNotificationService;
        this.expoPushNotificationService = expoPushNotificationService;
    }

    @Override
    public NotificationStatus getStatus() {
        PushNotificationStatusResponse status = pushNotificationService.getStatus();
        return new NotificationStatus(status.vapidConfigured(), status.vapidKeyPairValid(), status.publicKey());
    }

    @Override
    public NotificationDeliveryResult sendWeb(String userEmail, String title, String body, String url) {
        return toDomain(pushNotificationService.sendToUser(userEmail, title, body, url));
    }

    @Override
    public NotificationDeliveryResult sendMobile(String userEmail, String title, String body, String url) {
        return toDomain(expoPushNotificationService.sendToUser(userEmail, title, body, url));
    }

    private NotificationDeliveryResult toDomain(PushNotificationTestResponse response) {
        return new NotificationDeliveryResult(
                response.targetCount(),
                response.deliveredCount(),
                response.removedCount(),
                response.failedCount()
        );
    }
}
