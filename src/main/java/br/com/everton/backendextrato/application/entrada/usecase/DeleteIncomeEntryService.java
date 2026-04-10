package br.com.everton.backendextrato.application.entrada.usecase;

import br.com.everton.backendextrato.application.entrada.port.in.DeleteIncomeEntryUseCase;
import br.com.everton.backendextrato.application.entrada.port.out.IncomeEntryRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.entrada.IncomeEntry;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteIncomeEntryService implements DeleteIncomeEntryUseCase {

    private final IncomeEntryRepository incomeEntryRepository;

    public DeleteIncomeEntryService(IncomeEntryRepository incomeEntryRepository) {
        this.incomeEntryRepository = incomeEntryRepository;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INCOME_ENTRIES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.INCOME_ENTRY_BY_ID, allEntries = true)
    })
    public boolean execute(String ownerEmail, Long incomeEntryId) {
        IncomeEntry incomeEntry = incomeEntryRepository.findByIdAndOwnerEmail(incomeEntryId, ownerEmail).orElse(null);
        if (incomeEntry == null) {
            return false;
        }

        incomeEntryRepository.delete(incomeEntry);
        return true;
    }
}
