package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {

    private final PushNotificationService webPushNotificationService;
    private final ExpoPushNotificationService expoPushNotificationService;

    public NotificationDeliveryService(
            PushNotificationService webPushNotificationService,
            ExpoPushNotificationService expoPushNotificationService
    ) {
        this.webPushNotificationService = webPushNotificationService;
        this.expoPushNotificationService = expoPushNotificationService;
    }

    public PushNotificationTestResponse sendToUser(String userEmail, String title, String body, String url) {
        PushNotificationTestResponse webResult = webPushNotificationService.sendToUser(userEmail, title, body, url);
        PushNotificationTestResponse mobileResult = expoPushNotificationService.sendToUser(userEmail, title, body, url);

        return new PushNotificationTestResponse(
                webResult.targetCount() + mobileResult.targetCount(),
                webResult.deliveredCount() + mobileResult.deliveredCount(),
                webResult.removedCount() + mobileResult.removedCount(),
                webResult.failedCount() + mobileResult.failedCount()
        );
    }
}
