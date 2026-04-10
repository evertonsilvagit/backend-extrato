package br.com.everton.backendextrato.application.categoriadivida.port.out;

import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;

import java.util.List;
import java.util.Optional;

public interface DebtCategoryRepository {
    List<DebtCategory> findAllByOwnerEmail(String ownerEmail);
    Optional<DebtCategory> findByIdAndOwnerEmail(Long id, String ownerEmail);
    Optional<DebtCategory> findByOwnerEmailAndName(String ownerEmail, String name);
    DebtCategory save(DebtCategory category);
    void delete(DebtCategory category);
}
