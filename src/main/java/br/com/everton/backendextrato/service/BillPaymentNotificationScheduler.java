package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.BillPaymentNotificationRunResponse;
import br.com.everton.backendextrato.application.notification.port.in.TriggerDueBillNotificationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class BillPaymentNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentNotificationScheduler.class);

    private final TriggerDueBillNotificationsUseCase triggerDueBillNotificationsUseCase;
    private final ZoneId notificationZone;

    public BillPaymentNotificationScheduler(
            TriggerDueBillNotificationsUseCase triggerDueBillNotificationsUseCase,
            @Value("${notifications.bill-payment.zone:America/Sao_Paulo}") String notificationZone
    ) {
        this.triggerDueBillNotificationsUseCase = triggerDueBillNotificationsUseCase;
        this.notificationZone = ZoneId.of(notificationZone);
    }

    @Scheduled(
            cron = "${notifications.bill-payment.cron:0 0 9 * * *}",
            zone = "${notifications.bill-payment.zone:America/Sao_Paulo}"
    )
    public void sendTodayDueBillNotifications() {
        sendDueBillNotifications(LocalDate.now(notificationZone));
    }

    @Transactional
    public BillPaymentNotificationRunResponse sendDueBillNotifications(LocalDate referenceDate) {
        log.debug("Triggering due bill notifications for referenceDate={}", referenceDate);
        return triggerDueBillNotificationsUseCase.execute(referenceDate);
    }
}
