package br.com.everton.backendextrato.application.divida.port.out;

import br.com.everton.backendextrato.domain.divida.Debt;

import java.util.List;
import java.util.Optional;

public interface DebtRepository {
    Optional<Debt> findByIdAndOwnerEmail(Long id, String ownerEmail);
    List<Debt> findAllByOwnerEmail(String ownerEmail);
    Optional<Debt> findTopByOwnerEmailOrderBySortOrderDesc(String ownerEmail);
    Debt save(Debt debt);
    void delete(Debt debt);
}
