package br.com.everton.backendextrato.application.divida.usecase;

import br.com.everton.backendextrato.application.divida.port.in.ListDebtsUseCase;
import br.com.everton.backendextrato.application.divida.port.out.DebtRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.divida.Debt;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListDebtsService implements ListDebtsUseCase {

    private final DebtRepository debtRepository;

    public ListDebtsService(DebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.DEBTS, key = "#ownerEmail", unless = "#result == null || #result.isEmpty()")
    public List<Debt> execute(String ownerEmail) {
        return debtRepository.findAllByOwnerEmail(ownerEmail);
    }
}
