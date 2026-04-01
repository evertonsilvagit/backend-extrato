package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.BillPaymentNotificationRunResponse;
import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.model.BillPaymentNotificationLog;
import br.com.everton.backendextrato.model.Conta;
import br.com.everton.backendextrato.repository.BillPaymentNotificationLogRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillPaymentNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentNotificationScheduler.class);
    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    private final ContaRepository contaRepository;
    private final NotificationDeliveryService notificationDeliveryService;
    private final BillPaymentNotificationLogRepository notificationLogRepository;
    private final ZoneId notificationZone;

    public BillPaymentNotificationScheduler(
            ContaRepository contaRepository,
            NotificationDeliveryService notificationDeliveryService,
            BillPaymentNotificationLogRepository notificationLogRepository,
            @Value("${notifications.bill-payment.zone:America/Sao_Paulo}") String notificationZone
    ) {
        this.contaRepository = contaRepository;
        this.notificationDeliveryService = notificationDeliveryService;
        this.notificationLogRepository = notificationLogRepository;
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
        Map<String, List<Conta>> dueBillsByUser = contaRepository.findAll().stream()
                .filter(conta -> hasText(conta.getUserEmail()))
                .filter(conta -> isBillActiveInMonth(conta, referenceDate))
                .filter(conta -> isBillDueOn(conta, referenceDate))
                .collect(Collectors.groupingBy(conta -> conta.getUserEmail().trim().toLowerCase()));

        if (dueBillsByUser.isEmpty()) {
            log.debug("No due bills found for referenceDate={}", referenceDate);
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
            if (notificationLogRepository.existsByUserEmailIgnoreCaseAndReferenceDate(userEmail, referenceDate)) {
                log.debug("Skipping bill push because a notification was already sent for userEmail={} on {}", userEmail, referenceDate);
                skippedAlreadySentCount++;
                continue;
            }

            List<Conta> dueBills = entry.getValue();
            PushNotificationTestResponse result = notificationDeliveryService.sendToUser(
                    userEmail,
                    buildTitle(dueBills),
                    buildBody(dueBills, referenceDate),
                    "/contas"
            );

            if (result.targetCount() == 0) {
                usersWithoutSubscriptionsCount++;
                continue;
            }

            triggeredUserCount++;
            deliveredSubscriptionCount += result.deliveredCount();
            failedSubscriptionCount += result.failedCount();

            BillPaymentNotificationLog logEntry = new BillPaymentNotificationLog();
            logEntry.setUserEmail(userEmail);
            logEntry.setReferenceDate(referenceDate);
            notificationLogRepository.save(logEntry);
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

        int effectivePaymentDay = Math.min(conta.getDiaPagamento(), referenceDate.lengthOfMonth());
        return referenceDate.getDayOfMonth() == effectivePaymentDay;
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
                    safeText(bill.getDescricao()),
                    currencyFormatter.format(bill.getValor())
            );
        }

        String highlightedBills = dueBills.stream()
                .limit(2)
                .map(conta -> safeText(conta.getDescricao()))
                .collect(Collectors.joining(", "));
        String suffix = dueBills.size() > 2 ? " e outras " + (dueBills.size() - 2) + " conta(s)" : "";

        return String.format(
                LOCALE_PT_BR,
                "Hoje, %s, vencem %d contas: %s%s.",
                referenceDate,
                dueBills.size(),
                highlightedBills,
                suffix
        );
    }

    private String safeText(String value) {
        return hasText(value) ? value.trim() : "uma conta";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
