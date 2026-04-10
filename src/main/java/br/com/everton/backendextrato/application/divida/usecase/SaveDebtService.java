package br.com.everton.backendextrato.application.divida.usecase;

import br.com.everton.backendextrato.application.categoriadivida.port.out.DebtCategoryRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.application.divida.exception.DebtNotFoundException;
import br.com.everton.backendextrato.application.divida.exception.DebtValidationException;
import br.com.everton.backendextrato.application.divida.port.in.SaveDebtUseCase;
import br.com.everton.backendextrato.application.divida.port.out.DebtRepository;
import br.com.everton.backendextrato.application.divida.usecase.command.SaveDebtCommand;
import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;
import br.com.everton.backendextrato.domain.divida.Debt;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SaveDebtService implements SaveDebtUseCase {

    private final DebtRepository debtRepository;
    private final DebtCategoryRepository debtCategoryRepository;

    public SaveDebtService(DebtRepository debtRepository, DebtCategoryRepository debtCategoryRepository) {
        this.debtRepository = debtRepository;
        this.debtCategoryRepository = debtCategoryRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DEBTS, key = "#command.ownerEmail()")
    public Debt execute(SaveDebtCommand command) {
        validate(command);

        Debt existing = null;
        if (command.id() != null) {
            existing = debtRepository.findByIdAndOwnerEmail(command.id(), command.ownerEmail())
                    .orElseThrow(() -> new DebtNotFoundException("Divida nao encontrada para o usuario autenticado."));
        }

        DebtCategory category = debtCategoryRepository.findByOwnerEmailAndName(command.ownerEmail(), command.categoryName().trim())
                .orElseThrow(() -> new DebtValidationException("Categoria de divida invalida para o usuario autenticado."));

        return debtRepository.save(new Debt(
                command.id(),
                command.description().trim(),
                command.amount(),
                category.id(),
                category.name(),
                resolveOrder(command.ownerEmail(), existing, command.sortOrder()),
                command.ownerEmail()
        ));
    }

    private Integer resolveOrder(String ownerEmail, Debt existing, Integer requestedOrder) {
        if (requestedOrder != null) {
            return requestedOrder;
        }

        if (existing != null && existing.sortOrder() != null) {
            return existing.sortOrder();
        }

        return debtRepository.findTopByOwnerEmailOrderBySortOrderDesc(ownerEmail)
                .map(debt -> debt.sortOrder() + 1)
                .orElse(1);
    }

    private void validate(SaveDebtCommand command) {
        if (command == null) {
            throw new DebtValidationException("Payload obrigatorio.");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new DebtValidationException("Descricao obrigatoria");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new DebtValidationException("Valor invalido");
        }
        if (command.categoryName() == null || command.categoryName().isBlank()) {
            throw new DebtValidationException("Categoria obrigatoria");
        }
        if (command.sortOrder() != null && command.sortOrder() < 1) {
            throw new DebtValidationException("Ordem invalida");
        }
    }
}
