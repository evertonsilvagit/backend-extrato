package br.com.everton.backendextrato.application.notification.port.in;

import br.com.everton.backendextrato.application.notification.usecase.command.RegisterMobileSubscriptionCommand;
import br.com.everton.backendextrato.application.notification.usecase.result.MobileSubscriptionRegistrationResult;

public interface RegisterMobileSubscriptionUseCase {
    MobileSubscriptionRegistrationResult execute(RegisterMobileSubscriptionCommand command);
}
