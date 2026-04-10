package br.com.everton.backendextrato.application.notification.port.in;

import br.com.everton.backendextrato.application.notification.usecase.command.SendNotificationTestCommand;
import br.com.everton.backendextrato.domain.notification.NotificationDeliveryResult;

public interface SendNotificationTestUseCase {
    NotificationDeliveryResult execute(SendNotificationTestCommand command);
}
