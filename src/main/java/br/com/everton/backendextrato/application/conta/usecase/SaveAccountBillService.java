package br.com.everton.backendextrato.application.conta.usecase;

import br.com.everton.backendextrato.application.categoriaconta.port.out.AccountCategoryRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.application.conta.exception.AccountBillNotFoundException;
import br.com.everton.backendextrato.application.conta.exception.AccountBillValidationException;
import br.com.everton.backendextrato.application.conta.port.in.SaveAccountBillUseCase;
import br.com.everton.backendextrato.application.conta.port.out.AccountBillRepository;
import br.com.everton.backendextrato.application.conta.usecase.command.SaveAccountBillCommand;
import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;
import br.com.everton.backendextrato.domain.conta.AccountBill;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SaveAccountBillService implements SaveAccountBillUseCase {

    private final AccountBillRepository accountBillRepository;
    private final AccountCategoryRepository accountCategoryRepository;

    public SaveAccountBillService(
            AccountBillRepository accountBillRepository,
            AccountCategoryRepository accountCategoryRepository
    ) {
        this.accountBillRepository = accountBillRepository;
        this.accountCategoryRepository = accountCategoryRepository;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.ACCOUNT_BILLS, key = "#command.ownerEmail()"),
            @CacheEvict(cacheNames = CacheNames.ACCOUNT_BILL_BY_ID, allEntries = true)
    })
    public AccountBill execute(SaveAccountBillCommand command) {
        validate(command);

        AccountBill existing = null;
        Long previousCategoryId = null;
        Integer previousOrder = null;
        if (command.id() != null) {
            existing = accountBillRepository.findByIdAndOwnerEmail(command.id(), command.ownerEmail())
                    .orElseThrow(() -> new AccountBillNotFoundException("Conta nao encontrada para o usuario autenticado."));
            previousCategoryId = existing.categoryId();
            previousOrder = existing.sortOrder();
        }

        AccountCategory category = accountCategoryRepository.findByOwnerEmailAndName(command.ownerEmail(), command.categoryName().trim())
                .orElseThrow(() -> new AccountBillValidationException("Categoria de conta invalida para o usuario autenticado."));

        Integer resolvedOrder = resolveOrder(
                command.ownerEmail(),
                category.id(),
                command.id(),
                command.sortOrder(),
                previousCategoryId,
                previousOrder
        );

        AccountBill draft = new AccountBill(
                command.id(),
                command.description().trim(),
                command.amount(),
                command.paymentDay(),
                category.id(),
                category.name(),
                command.activeMonths(),
                command.ownerEmail(),
                resolvedOrder
        );

        AccountBill saved = accountBillRepository.save(draft);
        normalizeCategoryOrders(command.ownerEmail(), category.id(), saved.id(), saved.sortOrder());

        if (previousCategoryId != null && !previousCategoryId.equals(category.id())) {
            normalizeCategoryOrders(command.ownerEmail(), previousCategoryId, null, null);
        }

        return saved;
    }

    private Integer resolveOrder(
            String ownerEmail,
            Long categoryId,
            Long billId,
            Integer requestedOrder,
            Long previousCategoryId,
            Integer previousOrder
    ) {
        List<AccountBill> categoryBills = accountBillRepository.findAllByOwnerEmailAndCategoryId(ownerEmail, categoryId).stream()
                .filter(existing -> billId == null || !billId.equals(existing.id()))
                .toList();

        if (requestedOrder != null) {
            return clampOrder(requestedOrder, categoryBills.size() + 1);
        }

        if (previousOrder != null && previousCategoryId != null && previousCategoryId.equals(categoryId)) {
            return clampOrder(previousOrder, categoryBills.size() + 1);
        }

        return categoryBills.size() + 1;
    }

    private void normalizeCategoryOrders(String ownerEmail, Long categoryId, Long prioritizedBillId, Integer prioritizedOrder) {
        AccountBillOrderNormalizer.normalize(
                accountBillRepository.findAllByOwnerEmailAndCategoryId(ownerEmail, categoryId),
                prioritizedBillId,
                prioritizedOrder,
                accountBillRepository::saveAll
        );
    }

    private Integer clampOrder(Integer requestedOrder, int maxOrder) {
        if (requestedOrder == null) {
            return maxOrder;
        }
        return Math.max(1, Math.min(requestedOrder, maxOrder));
    }

    private void validate(SaveAccountBillCommand command) {
        if (command == null) {
            throw new AccountBillValidationException("payload obrigatorio");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new AccountBillValidationException("descricao e obrigatoria");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountBillValidationException("valor deve ser >= 0");
        }
        if (command.paymentDay() == null || command.paymentDay() < 1 || command.paymentDay() > 31) {
            throw new AccountBillValidationException("diaPagamento deve estar entre 1 e 31");
        }
        if (command.categoryName() == null || command.categoryName().isBlank()) {
            throw new AccountBillValidationException("categoria e obrigatoria");
        }
        if (command.sortOrder() != null && command.sortOrder() < 1) {
            throw new AccountBillValidationException("ordem deve ser >= 1");
        }
    }
}
