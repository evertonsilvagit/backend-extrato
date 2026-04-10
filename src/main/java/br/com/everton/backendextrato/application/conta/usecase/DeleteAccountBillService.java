package br.com.everton.backendextrato.application.conta.usecase;

import br.com.everton.backendextrato.application.conta.port.in.DeleteAccountBillUseCase;
import br.com.everton.backendextrato.application.conta.port.out.AccountBillRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.conta.AccountBill;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteAccountBillService implements DeleteAccountBillUseCase {

    private final AccountBillRepository accountBillRepository;

    public DeleteAccountBillService(AccountBillRepository accountBillRepository) {
        this.accountBillRepository = accountBillRepository;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.ACCOUNT_BILLS, key = "#ownerEmail"),
            @CacheEvict(cacheNames = CacheNames.ACCOUNT_BILL_BY_ID, allEntries = true)
    })
    public boolean execute(String ownerEmail, Long billId) {
        AccountBill bill = accountBillRepository.findByIdAndOwnerEmail(billId, ownerEmail).orElse(null);
        if (bill == null) {
            return false;
        }

        accountBillRepository.delete(bill);
        if (bill.categoryId() != null) {
            normalizeCategoryOrders(ownerEmail, bill.categoryId(), null, null);
        }
        return true;
    }

    private void normalizeCategoryOrders(String ownerEmail, Long categoryId, Long prioritizedBillId, Integer prioritizedOrder) {
        AccountBillOrderNormalizer.normalize(
                accountBillRepository.findAllByOwnerEmailAndCategoryId(ownerEmail, categoryId),
                prioritizedBillId,
                prioritizedOrder,
                accountBillRepository::saveAll
        );
    }
}
