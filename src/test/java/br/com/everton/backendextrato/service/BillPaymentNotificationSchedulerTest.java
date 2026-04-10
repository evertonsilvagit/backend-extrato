package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.application.notification.port.in.TriggerDueBillNotificationsUseCase;
import br.com.everton.backendextrato.dto.BillPaymentNotificationRunResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillPaymentNotificationSchedulerTest {

    @Mock
    private TriggerDueBillNotificationsUseCase triggerDueBillNotificationsUseCase;

    private BillPaymentNotificationScheduler scheduler;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        scheduler = new BillPaymentNotificationScheduler(
                triggerDueBillNotificationsUseCase,
                "America/Sao_Paulo"
        );
    }

    @Test
    void shouldDelegateDueBillNotificationExecution() {
        LocalDate referenceDate = LocalDate.of(2026, 3, 28);
        BillPaymentNotificationRunResponse expected = new BillPaymentNotificationRunResponse(referenceDate, 1, 2, 1, 0, 0, 1, 0);
        when(triggerDueBillNotificationsUseCase.execute(referenceDate)).thenReturn(expected);

        BillPaymentNotificationRunResponse response = scheduler.sendDueBillNotifications(referenceDate);

        verify(triggerDueBillNotificationsUseCase).execute(referenceDate);
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldUseConfiguredZoneWhenSchedulingTodayNotifications() {
        LocalDate referenceDate = LocalDate.of(2026, 2, 28);
        when(triggerDueBillNotificationsUseCase.execute(referenceDate)).thenReturn(
                new BillPaymentNotificationRunResponse(referenceDate, 0, 0, 0, 0, 0, 0, 0)
        );

        BillPaymentNotificationRunResponse response = scheduler.sendDueBillNotifications(referenceDate);

        verify(triggerDueBillNotificationsUseCase).execute(referenceDate);
        assertThat(response.referenceDate()).isEqualTo(referenceDate);
    }
}
