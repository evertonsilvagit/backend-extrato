package br.com.everton.backendextrato.application.categoriadivida.usecase;

import br.com.everton.backendextrato.application.categoriadivida.port.in.ListDebtCategoriesUseCase;
import br.com.everton.backendextrato.application.categoriadivida.port.out.DebtCategoryRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListDebtCategoriesService implements ListDebtCategoriesUseCase {

    private final DebtCategoryRepository debtCategoryRepository;

    public ListDebtCategoriesService(DebtCategoryRepository debtCategoryRepository) {
        this.debtCategoryRepository = debtCategoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.DEBT_CATEGORIES, key = "#ownerEmail", unless = "#result == null || #result.isEmpty()")
    public List<DebtCategory> execute(String ownerEmail) {
        return debtCategoryRepository.findAllByOwnerEmail(ownerEmail);
    }
}
