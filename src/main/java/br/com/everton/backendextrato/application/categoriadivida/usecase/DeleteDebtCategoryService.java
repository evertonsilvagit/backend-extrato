package br.com.everton.backendextrato.application.categoriadivida.usecase;

import br.com.everton.backendextrato.application.categoriadivida.exception.DebtCategoryConflictException;
import br.com.everton.backendextrato.application.categoriadivida.port.in.DeleteDebtCategoryUseCase;
import br.com.everton.backendextrato.application.categoriadivida.port.out.DebtCategoryRepository;
import br.com.everton.backendextrato.application.categoriadivida.port.out.DebtCategoryUsageRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteDebtCategoryService implements DeleteDebtCategoryUseCase {

    private final DebtCategoryRepository debtCategoryRepository;
    private final DebtCategoryUsageRepository debtCategoryUsageRepository;

    public DeleteDebtCategoryService(
            DebtCategoryRepository debtCategoryRepository,
            DebtCategoryUsageRepository debtCategoryUsageRepository
    ) {
        this.debtCategoryRepository = debtCategoryRepository;
        this.debtCategoryUsageRepository = debtCategoryUsageRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DEBT_CATEGORIES, key = "#ownerEmail")
    public boolean execute(String ownerEmail, Long categoryId) {
        DebtCategory category = debtCategoryRepository.findByIdAndOwnerEmail(categoryId, ownerEmail).orElse(null);
        if (category == null) {
            return false;
        }

        if (debtCategoryUsageRepository.isInUse(ownerEmail, categoryId)) {
            throw new DebtCategoryConflictException("Não é possível excluir uma categoria de dívida que está em uso.");
        }

        debtCategoryRepository.delete(category);
        return true;
    }
}
