package br.com.everton.backendextrato.dto;

import java.time.LocalDate;

public record BillPaymentNotificationRunResponse(
        LocalDate referenceDate,
        int dueUserCount,
        int dueBillCount,
        int triggeredUserCount,
        int skippedAlreadySentCount,
        int usersWithoutSubscriptionsCount,
        int deliveredSubscriptionCount,
        int failedSubscriptionCount
) {
}
