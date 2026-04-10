package br.com.everton.backendextrato.application.notification.port.out;

import br.com.everton.backendextrato.domain.notification.NotificationDeliveryResult;
import br.com.everton.backendextrato.domain.notification.NotificationStatus;

public interface NotificationDeliveryPort {
    NotificationStatus getStatus();
    NotificationDeliveryResult sendWeb(String userEmail, String title, String body, String url);
    NotificationDeliveryResult sendMobile(String userEmail, String title, String body, String url);
}
