package br.com.everton.backendextrato.domain.notification;

public record NotificationDeliveryResult(
        int targetCount,
        int deliveredCount,
        int removedCount,
        int failedCount
) {
    public NotificationDeliveryResult plus(NotificationDeliveryResult other) {
        return new NotificationDeliveryResult(
                targetCount + other.targetCount,
                deliveredCount + other.deliveredCount,
                removedCount + other.removedCount,
                failedCount + other.failedCount
        );
    }
}
