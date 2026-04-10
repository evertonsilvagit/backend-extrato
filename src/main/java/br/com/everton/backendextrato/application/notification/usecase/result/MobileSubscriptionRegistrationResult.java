package br.com.everton.backendextrato.application.notification.usecase.result;

import br.com.everton.backendextrato.domain.notification.MobileSubscription;

public record MobileSubscriptionRegistrationResult(
        MobileSubscription subscription,
        boolean created
) {}
