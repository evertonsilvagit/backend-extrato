package br.com.everton.backendextrato.application.entrada.usecase;

import br.com.everton.backendextrato.application.entrada.exception.IncomeEntryNotFoundException;
import br.com.everton.backendextrato.application.entrada.exception.IncomeEntryValidationException;
import br.com.everton.backendextrato.application.entrada.port.in.SaveIncomeEntryUseCase;
import br.com.everton.backendextrato.application.entrada.port.out.IncomeEntryRepository;
import br.com.everton.backendextrato.application.entrada.usecase.command.SaveIncomeEntryCommand;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.entrada.IncomeEntry;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SaveIncomeEntryService implements SaveIncomeEntryUseCase {
    private static final List<String> VALID_INCOME_CATEGORIES = List.of(
            "STANDARD",
            "SALARY_ADVANCE",
            "SALARY_SETTLEMENT"
    );

    private final IncomeEntryRepository incomeEntryRepository;

    public SaveIncomeEntryService(IncomeEntryRepository incomeEntryRepository) {
        this.incomeEntryRepository = incomeEntryRepository;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INCOME_ENTRIES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.INCOME_ENTRY_BY_ID, allEntries = true)
    })
    public IncomeEntry execute(SaveIncomeEntryCommand command) {
        validate(command);

        IncomeEntry existing = null;
        if (command.id() != null) {
            existing = incomeEntryRepository.findByIdAndOwnerEmail(command.id(), command.ownerEmail())
                    .orElseThrow(() -> new IncomeEntryNotFoundException("Entrada nao encontrada para o usuario autenticado."));
        }

        return incomeEntryRepository.save(new IncomeEntry(
                command.id(),
                command.name(),
                command.type(),
                command.amount(),
                command.taxRate(),
                command.paymentDays(),
                Boolean.TRUE.equals(command.netAmount()),
                normalizeIncomeCategory(command.incomeCategory()),
                command.activeMonths(),
                resolveOrder(command.ownerEmail(), existing, command.sortOrder()),
                command.ownerEmail()
        ));
    }

    private Integer resolveOrder(String ownerEmail, IncomeEntry existing, Integer requestedOrder) {
        if (requestedOrder != null) {
            return requestedOrder;
        }

        if (existing != null && existing.sortOrder() != null) {
            return existing.sortOrder();
        }

        return incomeEntryRepository.findTopByOwnerEmailOrderBySortOrderDesc(ownerEmail)
                .map(entry -> entry.sortOrder() + 1)
                .orElse(1);
    }

    private void validate(SaveIncomeEntryCommand command) {
        if (command == null) {
            throw new IncomeEntryValidationException("payload obrigatorio");
        }
        if (command.name() == null || command.name().isBlank()) {
            throw new IncomeEntryValidationException("nome e obrigatorio");
        }
        if (command.type() == null || command.type().isBlank()) {
            throw new IncomeEntryValidationException("tipo e obrigatorio");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IncomeEntryValidationException("valor deve ser > 0");
        }
        if (command.taxRate() == null || command.taxRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IncomeEntryValidationException("taxaImposto deve ser >= 0");
        }
        if (command.paymentDays() != null) {
            boolean validDays = command.paymentDays().stream().allMatch(day -> day != null && day >= 1 && day <= 31);
            if (!validDays) {
                throw new IncomeEntryValidationException("diasRecebimento deve conter valores entre 1 e 31");
            }
        }
        if (!VALID_INCOME_CATEGORIES.contains(normalizeIncomeCategory(command.incomeCategory()))) {
            throw new IncomeEntryValidationException("categoriaRecebimento invalida");
        }
        if (command.activeMonths() == null || command.activeMonths().isEmpty()) {
            throw new IncomeEntryValidationException("mesesVigencia e obrigatorio");
        }
        boolean validMonths = command.activeMonths().stream().allMatch(month -> month != null && month >= 1 && month <= 12);
        if (!validMonths) {
            throw new IncomeEntryValidationException("mesesVigencia deve conter valores entre 1 e 12");
        }
        if (command.sortOrder() != null && command.sortOrder() < 1) {
            throw new IncomeEntryValidationException("ordem deve ser >= 1");
        }
    }

    private String normalizeIncomeCategory(String incomeCategory) {
        if (incomeCategory == null || incomeCategory.isBlank()) {
            return "STANDARD";
        }

        return incomeCategory.trim().toUpperCase();
    }
}
