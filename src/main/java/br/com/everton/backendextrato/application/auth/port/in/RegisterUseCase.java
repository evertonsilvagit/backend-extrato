package br.com.everton.backendextrato.application.auth.port.in;

import br.com.everton.backendextrato.application.auth.usecase.command.AuthCommand;
import br.com.everton.backendextrato.application.auth.usecase.result.AuthSession;

public interface RegisterUseCase {
    AuthSession execute(AuthCommand command);
}
