package br.com.everton.backendextrato.application.notification.usecase;

import br.com.everton.backendextrato.application.notification.port.in.TriggerDueBillNotificationsUseCase;
import br.com.everton.backendextrato.application.notification.port.out.NotificationDeliveryPort;
import br.com.everton.backendextrato.application.notification.port.out.NotificationLogPort;
import br.com.everton.backendextrato.domain.notification.NotificationDeliveryResult;
import br.com.everton.backendextrato.dto.BillPaymentNotificationRunResponse;
import br.com.everton.backendextrato.repository.ContaRepository;
import br.com.everton.backendextrato.model.Conta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TriggerDueBillNotificationsService implements TriggerDueBillNotificationsUseCase {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private final ContaRepository contaRepository;
    private final NotificationDeliveryPort notificationDeliveryPort;
    private final NotificationLogPort notificationLogPort;

    public TriggerDueBillNotificationsService(
            ContaRepository contaRepository,
            NotificationDeliveryPort notificationDeliveryPort,
            NotificationLogPort notificationLogPort
    ) {
        this.contaRepository = contaRepository;
        this.notificationDeliveryPort = notificationDeliveryPort;
        this.notificationLogPort = notificationLogPort;
    }

    @Override
    @Transactional
    public BillPaymentNotificationRunResponse execute(LocalDate referenceDate) {
        Map<String, List<Conta>> dueBillsByUser = contaRepository.findAll().stream()
                .filter(conta -> conta.getUserEmail() != null && !conta.getUserEmail().isBlank())
                .filter(conta -> isBillActiveInMonth(conta, referenceDate))
                .filter(conta -> isBillDueOn(conta, referenceDate))
                .collect(Collectors.groupingBy(conta -> conta.getUserEmail().trim().toLowerCase()));

        if (dueBillsByUser.isEmpty()) {
            return new BillPaymentNotificationRunResponse(referenceDate, 0, 0, 0, 0, 0, 0, 0);
        }

        int dueBillCount = dueBillsByUser.values().stream().mapToInt(List::size).sum();
        int triggeredUserCount = 0;
        int skippedAlreadySentCount = 0;
        int usersWithoutSubscriptionsCount = 0;
        int deliveredSubscriptionCount = 0;
        int failedSubscriptionCount = 0;

        for (Map.Entry<String, List<Conta>> entry : dueBillsByUser.entrySet()) {
            String userEmail = entry.getKey();
            if (notificationLogPort.existsForUserAndDate(userEmail, referenceDate)) {
                skippedAlreadySentCount++;
                continue;
            }

            NotificationDeliveryResult result = notificationDeliveryPort.sendWeb(
                    userEmail,
                    buildTitle(entry.getValue()),
                    buildBody(entry.getValue(), referenceDate),
                    "/contas"
            ).plus(notificationDeliveryPort.sendMobile(
                    userEmail,
                    buildTitle(entry.getValue()),
                    buildBody(entry.getValue(), referenceDate),
                    "/contas"
            ));

            if (result.targetCount() == 0) {
                usersWithoutSubscriptionsCount++;
                continue;
            }

            triggeredUserCount++;
            deliveredSubscriptionCount += result.deliveredCount();
            failedSubscriptionCount += result.failedCount();
            notificationLogPort.save(userEmail, referenceDate);
        }

        return new BillPaymentNotificationRunResponse(
                referenceDate,
                dueBillsByUser.size(),
                dueBillCount,
                triggeredUserCount,
                skippedAlreadySentCount,
                usersWithoutSubscriptionsCount,
                deliveredSubscriptionCount,
                failedSubscriptionCount
        );
    }

    private boolean isBillActiveInMonth(Conta conta, LocalDate referenceDate) {
        List<Integer> mesesVigencia = conta.getMesesVigencia();
        return mesesVigencia == null || mesesVigencia.isEmpty() || mesesVigencia.contains(referenceDate.getMonthValue());
    }

    private boolean isBillDueOn(Conta conta, LocalDate referenceDate) {
        if (conta.getDiaPagamento() == null) {
            return false;
        }
        return referenceDate.getDayOfMonth() == Math.min(conta.getDiaPagamento(), referenceDate.lengthOfMonth());
    }

    private String buildTitle(List<Conta> dueBills) {
        return dueBills.size() == 1 ? "Conta para pagar hoje" : "Contas para pagar hoje";
    }

    private String buildBody(List<Conta> dueBills, LocalDate referenceDate) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
        if (dueBills.size() == 1) {
            Conta bill = dueBills.get(0);
            return String.format(
                    LOCALE_PT_BR,
                    "Hoje, %s, vence %s no valor de %s.",
                    referenceDate,
                    bill.getDescricao() == null || bill.getDescricao().isBlank() ? "uma conta" : bill.getDescricao().trim(),
                    currencyFormatter.format(bill.getValor())
            );
        }

        String highlightedBills = dueBills.stream()
                .limit(2)
                .map(conta -> conta.getDescricao() == null || conta.getDescricao().isBlank() ? "uma conta" : conta.getDescricao().trim())
                .collect(Collectors.joining(", "));
        String suffix = dueBills.size() > 2 ? " e outras " + (dueBills.size() - 2) + " conta(s)" : "";
        return String.format(LOCALE_PT_BR, "Hoje, %s, vencem %d contas: %s%s.", referenceDate, dueBills.size(), highlightedBills, suffix);
    }
}
