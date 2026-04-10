package br.com.everton.backendextrato.application.entrada.port.out;

import br.com.everton.backendextrato.domain.entrada.IncomeEntry;

import java.util.List;
import java.util.Optional;

public interface IncomeEntryRepository {
    Optional<IncomeEntry> findByIdAndOwnerEmail(Long id, String ownerEmail);
    List<IncomeEntry> findAllByOwnerEmail(String ownerEmail, int page, int size);
    Optional<IncomeEntry> findTopByOwnerEmailOrderBySortOrderDesc(String ownerEmail);
    IncomeEntry save(IncomeEntry incomeEntry);
    void delete(IncomeEntry incomeEntry);
}
