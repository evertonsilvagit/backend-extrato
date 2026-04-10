package br.com.everton.backendextrato.application.notification.port.in;

import br.com.everton.backendextrato.application.notification.usecase.command.RegisterWebSubscriptionCommand;
import br.com.everton.backendextrato.application.notification.usecase.result.WebSubscriptionRegistrationResult;

public interface RegisterWebSubscriptionUseCase {
    WebSubscriptionRegistrationResult execute(RegisterWebSubscriptionCommand command);
}
