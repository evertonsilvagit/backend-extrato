package br.com.everton.backendextrato.application.categoriaconta.usecase;

import br.com.everton.backendextrato.application.categoriaconta.port.in.ListAccountCategoriesUseCase;
import br.com.everton.backendextrato.application.categoriaconta.port.out.AccountCategoryRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAccountCategoriesService implements ListAccountCategoriesUseCase {

    private final AccountCategoryRepository accountCategoryRepository;

    public ListAccountCategoriesService(AccountCategoryRepository accountCategoryRepository) {
        this.accountCategoryRepository = accountCategoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.ACCOUNT_CATEGORIES, key = "#ownerEmail", unless = "#result == null || #result.isEmpty()")
    public List<AccountCategory> execute(String ownerEmail) {
        return accountCategoryRepository.findAllByOwnerEmail(ownerEmail);
    }
}
