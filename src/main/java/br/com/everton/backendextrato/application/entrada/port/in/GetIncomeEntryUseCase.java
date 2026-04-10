package br.com.everton.backendextrato.application.entrada.port.in;

import br.com.everton.backendextrato.domain.entrada.IncomeEntry;

import java.util.Optional;

public interface GetIncomeEntryUseCase {
    Optional<IncomeEntry> execute(String ownerEmail, Long incomeEntryId);
}
