package br.com.everton.backendextrato.application.categoriadivida.usecase;

import br.com.everton.backendextrato.application.categoriadivida.exception.DebtCategoryConflictException;
import br.com.everton.backendextrato.application.categoriadivida.exception.DebtCategoryNotFoundException;
import br.com.everton.backendextrato.application.categoriadivida.exception.DebtCategoryValidationException;
import br.com.everton.backendextrato.application.categoriadivida.port.in.SaveDebtCategoryUseCase;
import br.com.everton.backendextrato.application.categoriadivida.port.out.DebtCategoryRepository;
import br.com.everton.backendextrato.application.categoriadivida.usecase.command.SaveDebtCategoryCommand;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveDebtCategoryService implements SaveDebtCategoryUseCase {

    private final DebtCategoryRepository debtCategoryRepository;

    public SaveDebtCategoryService(DebtCategoryRepository debtCategoryRepository) {
        this.debtCategoryRepository = debtCategoryRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DEBT_CATEGORIES, key = "#command.ownerEmail()")
    public DebtCategory execute(SaveDebtCategoryCommand command) {
        validate(command);

        DebtCategory draft = new DebtCategory(command.id(), command.name(), command.ownerEmail());
        DebtCategory category = loadExistingOrCreate(draft);

        debtCategoryRepository.findByOwnerEmailAndName(command.ownerEmail(), draft.name())
                .filter(existing -> !existing.id().equals(category.id()))
                .ifPresent(existing -> {
                    throw new DebtCategoryConflictException("Já existe uma categoria de dívida com este nome.");
                });

        return debtCategoryRepository.save(category);
    }

    private DebtCategory loadExistingOrCreate(DebtCategory draft) {
        if (draft.id() == null) {
            return draft;
        }

        return debtCategoryRepository.findByIdAndOwnerEmail(draft.id(), draft.ownerEmail())
                .map(existing -> new DebtCategory(existing.id(), draft.name(), existing.ownerEmail()))
                .orElseThrow(() -> new DebtCategoryNotFoundException("Categoria de dívida não encontrada para o usuário autenticado."));
    }

    private void validate(SaveDebtCategoryCommand command) {
        if (command == null) {
            throw new DebtCategoryValidationException("Payload obrigatório.");
        }
    }
}
