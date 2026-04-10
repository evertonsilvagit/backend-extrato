package br.com.everton.backendextrato.application.entrada.usecase;

import br.com.everton.backendextrato.application.entrada.port.in.ListIncomeEntriesUseCase;
import br.com.everton.backendextrato.application.entrada.port.out.IncomeEntryRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.entrada.IncomeEntry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListIncomeEntriesService implements ListIncomeEntriesUseCase {

    private final IncomeEntryRepository incomeEntryRepository;

    public ListIncomeEntriesService(IncomeEntryRepository incomeEntryRepository) {
        this.incomeEntryRepository = incomeEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.INCOME_ENTRIES, key = "#ownerEmail + ':' + (#page == null ? 0 : #page) + ':' + (#size == null ? 20 : #size)", unless = "#result == null || #result.isEmpty()")
    public List<IncomeEntry> execute(String ownerEmail, Integer page, Integer size) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;
        return incomeEntryRepository.findAllByOwnerEmail(ownerEmail, safePage, safeSize);
    }
}
