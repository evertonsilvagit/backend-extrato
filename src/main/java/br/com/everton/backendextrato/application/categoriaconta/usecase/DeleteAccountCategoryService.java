package br.com.everton.backendextrato.application.categoriaconta.usecase;

import br.com.everton.backendextrato.application.categoriaconta.exception.AccountCategoryConflictException;
import br.com.everton.backendextrato.application.categoriaconta.port.in.DeleteAccountCategoryUseCase;
import br.com.everton.backendextrato.application.categoriaconta.port.out.AccountCategoryRepository;
import br.com.everton.backendextrato.application.categoriaconta.port.out.AccountCategoryUsageRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteAccountCategoryService implements DeleteAccountCategoryUseCase {

    private final AccountCategoryRepository accountCategoryRepository;
    private final AccountCategoryUsageRepository accountCategoryUsageRepository;

    public DeleteAccountCategoryService(
            AccountCategoryRepository accountCategoryRepository,
            AccountCategoryUsageRepository accountCategoryUsageRepository
    ) {
        this.accountCategoryRepository = accountCategoryRepository;
        this.accountCategoryUsageRepository = accountCategoryUsageRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.ACCOUNT_CATEGORIES, key = "#ownerEmail")
    public boolean execute(String ownerEmail, Long categoryId) {
        AccountCategory category = accountCategoryRepository.findByIdAndOwnerEmail(categoryId, ownerEmail).orElse(null);
        if (category == null) {
            return false;
        }

        if (accountCategoryUsageRepository.isInUse(ownerEmail, categoryId)) {
            throw new AccountCategoryConflictException("Não é possível excluir uma categoria de conta que está em uso.");
        }

        accountCategoryRepository.delete(category);
        return true;
    }
}
