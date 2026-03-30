package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.BillPaymentNotificationRunResponse;
import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.model.Conta;
import br.com.everton.backendextrato.repository.BillPaymentNotificationLogRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillPaymentNotificationSchedulerTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private BillPaymentNotificationLogRepository notificationLogRepository;

    private BillPaymentNotificationScheduler scheduler;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        scheduler = new BillPaymentNotificationScheduler(
                contaRepository,
                pushNotificationService,
                notificationLogRepository,
                "America/Sao_Paulo"
        );
    }

    @Test
    void shouldSendOneNotificationPerUserForBillsDueToday() {
        LocalDate referenceDate = LocalDate.of(2026, 3, 28);
        when(contaRepository.findAll()).thenReturn(List.of(
                buildConta("user@example.com", "Internet", 28, List.of(3), "89.90"),
                buildConta("user@example.com", "Energia", 28, List.of(3), "120.00"),
                buildConta("user@example.com", "Assinatura", 15, List.of(3), "19.90"),
                buildConta("other@example.com", "Aluguel", 28, List.of(4), "900.00"),
                buildConta(null, "Sem Usuario", 28, List.of(3), "10.00")
        ));
        when(notificationLogRepository.existsByUserEmailIgnoreCaseAndReferenceDate("user@example.com", referenceDate))
                .thenReturn(false);
        when(pushNotificationService.sendToUser(eq("user@example.com"), any(), any(), eq("/contas")))
                .thenReturn(new PushNotificationTestResponse(1, 1, 0, 0));

        BillPaymentNotificationRunResponse response = scheduler.sendDueBillNotifications(referenceDate);

        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(pushNotificationService).sendToUser(eq("user@example.com"), titleCaptor.capture(), bodyCaptor.capture(), eq("/contas"));
        verify(notificationLogRepository).save(any());

        assertThat(titleCaptor.getValue()).isEqualTo("Contas para pagar hoje");
        assertThat(bodyCaptor.getValue()).contains("vencem 2 contas");
        assertThat(bodyCaptor.getValue()).contains("Internet");
        assertThat(bodyCaptor.getValue()).contains("Energia");
        assertThat(response.referenceDate()).isEqualTo(referenceDate);
        assertThat(response.dueUserCount()).isEqualTo(1);
        assertThat(response.dueBillCount()).isEqualTo(2);
        assertThat(response.triggeredUserCount()).isEqualTo(1);
        assertThat(response.skippedAlreadySentCount()).isZero();
        assertThat(response.usersWithoutSubscriptionsCount()).isZero();
        assertThat(response.deliveredSubscriptionCount()).isEqualTo(1);
        assertThat(response.failedSubscriptionCount()).isZero();
    }

    @Test
    void shouldSkipUsersAlreadyNotifiedOnReferenceDate() {
        LocalDate referenceDate = LocalDate.of(2026, 2, 28);
        when(contaRepository.findAll()).thenReturn(List.of(
                buildConta("user@example.com", "Cartao", 31, List.of(2), "300.00")
        ));
        when(notificationLogRepository.existsByUserEmailIgnoreCaseAndReferenceDate("user@example.com", referenceDate))
                .thenReturn(true);

        BillPaymentNotificationRunResponse response = scheduler.sendDueBillNotifications(referenceDate);

        verify(pushNotificationService, never()).sendToUser(any(), any(), any(), any());
        verify(notificationLogRepository, never()).save(any());
        assertThat(response.referenceDate()).isEqualTo(referenceDate);
        assertThat(response.dueUserCount()).isEqualTo(1);
        assertThat(response.dueBillCount()).isEqualTo(1);
        assertThat(response.triggeredUserCount()).isZero();
        assertThat(response.skippedAlreadySentCount()).isEqualTo(1);
        assertThat(response.usersWithoutSubscriptionsCount()).isZero();
        assertThat(response.deliveredSubscriptionCount()).isZero();
        assertThat(response.failedSubscriptionCount()).isZero();
    }

    private Conta buildConta(String userEmail, String descricao, int diaPagamento, List<Integer> mesesVigencia, String valor) {
        Conta conta = new Conta();
        conta.setUserEmail(userEmail);
        conta.setDescricao(descricao);
        conta.setDiaPagamento(diaPagamento);
        conta.setMesesVigencia(mesesVigencia);
        conta.setValor(new BigDecimal(valor));
        return conta;
    }
}
