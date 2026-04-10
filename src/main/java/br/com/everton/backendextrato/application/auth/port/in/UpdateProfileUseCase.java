package br.com.everton.backendextrato.application.auth.port.in;

import br.com.everton.backendextrato.application.auth.usecase.command.UpdateProfileCommand;
import br.com.everton.backendextrato.application.auth.usecase.result.AuthSession;

public interface UpdateProfileUseCase {
    AuthSession execute(String email, UpdateProfileCommand command);
}
