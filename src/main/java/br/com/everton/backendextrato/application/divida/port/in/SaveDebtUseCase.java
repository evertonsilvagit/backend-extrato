package br.com.everton.backendextrato.application.divida.port.in;

import br.com.everton.backendextrato.application.divida.usecase.command.SaveDebtCommand;
import br.com.everton.backendextrato.domain.divida.Debt;

public interface SaveDebtUseCase {
    Debt execute(SaveDebtCommand command);
}
