package br.com.everton.backendextrato.application.notification.port.in;

import br.com.everton.backendextrato.dto.BillPaymentNotificationRunResponse;

import java.time.LocalDate;

public interface TriggerDueBillNotificationsUseCase {
    BillPaymentNotificationRunResponse execute(LocalDate referenceDate);
}
