package br.com.everton.backendextrato.domain.categoriadivida;

import br.com.everton.backendextrato.application.categoriadivida.exception.DebtCategoryValidationException;

public record DebtCategory(Long id, String name, String ownerEmail) {

    public DebtCategory {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new DebtCategoryValidationException("Owner email is required.");
        }

        if (name == null || name.isBlank()) {
            throw new DebtCategoryValidationException("Nome da categoria é obrigatório.");
        }

        name = name.trim();
        ownerEmail = ownerEmail.trim();
    }
}
