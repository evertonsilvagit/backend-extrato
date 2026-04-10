package br.com.everton.backendextrato.application.notification.usecase.result;

import br.com.everton.backendextrato.domain.notification.WebPushSubscription;

public record WebSubscriptionRegistrationResult(
        WebPushSubscription subscription,
        boolean created
) {}
