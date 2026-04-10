package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {

    private final PushNotificationService webPushNotificationService;
    private final ExpoPushNotificationService expoPushNotificationService;
    private final MongoNotificationLogService notificationLogService;

    public NotificationDeliveryService(
            PushNotificationService webPushNotificationService,
            ExpoPushNotificationService expoPushNotificationService,
            MongoNotificationLogService notificationLogService
    ) {
        this.webPushNotificationService = webPushNotificationService;
        this.expoPushNotificationService = expoPushNotificationService;
        this.notificationLogService = notificationLogService;
    }

    public PushNotificationTestResponse sendToUser(String userEmail, String title, String body, String url) {
        PushNotificationTestResponse webResult = webPushNotificationService.sendToUser(userEmail, title, body, url);
        PushNotificationTestResponse mobileResult = expoPushNotificationService.sendToUser(userEmail, title, body, url);

        notificationLogService.record(userEmail, title, body, url, webResult, mobileResult);

        return new PushNotificationTestResponse(
                webResult.targetCount() + mobileResult.targetCount(),
                webResult.deliveredCount() + mobileResult.deliveredCount(),
                webResult.removedCount() + mobileResult.removedCount(),
                webResult.failedCount() + mobileResult.failedCount()
        );
    }
}
