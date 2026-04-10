package br.com.everton.backendextrato.application.entrada.port.in;

import br.com.everton.backendextrato.application.entrada.usecase.command.SaveIncomeEntryCommand;
import br.com.everton.backendextrato.domain.entrada.IncomeEntry;

public interface SaveIncomeEntryUseCase {
    IncomeEntry execute(SaveIncomeEntryCommand command);
}
