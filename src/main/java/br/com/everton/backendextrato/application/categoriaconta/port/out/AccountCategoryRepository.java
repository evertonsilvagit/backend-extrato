package br.com.everton.backendextrato.application.categoriaconta.port.out;

import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;

import java.util.List;
import java.util.Optional;

public interface AccountCategoryRepository {
    List<AccountCategory> findAllByOwnerEmail(String ownerEmail);
    Optional<AccountCategory> findByIdAndOwnerEmail(Long id, String ownerEmail);
    Optional<AccountCategory> findByOwnerEmailAndName(String ownerEmail, String name);
    AccountCategory save(AccountCategory category);
    void delete(AccountCategory category);
}
