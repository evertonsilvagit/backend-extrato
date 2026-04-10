package br.com.everton.backendextrato.application.entrada.port.in;

import br.com.everton.backendextrato.domain.entrada.IncomeEntry;

import java.util.List;

public interface ListIncomeEntriesUseCase {
    List<IncomeEntry> execute(String ownerEmail, Integer page, Integer size);
}
