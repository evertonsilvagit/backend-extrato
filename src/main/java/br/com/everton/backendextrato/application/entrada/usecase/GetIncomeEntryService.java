package br.com.everton.backendextrato.application.entrada.usecase;

import br.com.everton.backendextrato.application.entrada.port.in.GetIncomeEntryUseCase;
import br.com.everton.backendextrato.application.entrada.port.out.IncomeEntryRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.entrada.IncomeEntry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetIncomeEntryService implements GetIncomeEntryUseCase {

    private final IncomeEntryRepository incomeEntryRepository;

    public GetIncomeEntryService(IncomeEntryRepository incomeEntryRepository) {
        this.incomeEntryRepository = incomeEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.INCOME_ENTRY_BY_ID, key = "#ownerEmail + ':' + #incomeEntryId", unless = "#result == null")
    public Optional<IncomeEntry> execute(String ownerEmail, Long incomeEntryId) {
        return incomeEntryRepository.findByIdAndOwnerEmail(incomeEntryId, ownerEmail);
    }
}
