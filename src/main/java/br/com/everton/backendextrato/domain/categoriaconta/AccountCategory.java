package br.com.everton.backendextrato.domain.categoriaconta;

import br.com.everton.backendextrato.application.categoriaconta.exception.AccountCategoryValidationException;

public record AccountCategory(Long id, String name, String ownerEmail) {

    public AccountCategory {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new AccountCategoryValidationException("Owner email is required.");
        }

        if (name == null || name.isBlank()) {
            throw new AccountCategoryValidationException("Nome da categoria é obrigatório.");
        }

        name = name.trim();
        ownerEmail = ownerEmail.trim();
    }

    public AccountCategory withId(Long newId) {
        return new AccountCategory(newId, name, ownerEmail);
    }
}
