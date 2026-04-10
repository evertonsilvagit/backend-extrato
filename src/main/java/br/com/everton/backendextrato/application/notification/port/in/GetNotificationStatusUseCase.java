package br.com.everton.backendextrato.application.notification.port.in;

import br.com.everton.backendextrato.domain.notification.NotificationStatus;

public interface GetNotificationStatusUseCase {
    NotificationStatus execute();
}
