package br.com.everton.backendextrato.application.conta.usecase;

import br.com.everton.backendextrato.application.conta.port.in.GetAccountBillUseCase;
import br.com.everton.backendextrato.application.conta.port.out.AccountBillRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.conta.AccountBill;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetAccountBillService implements GetAccountBillUseCase {

    private final AccountBillRepository accountBillRepository;

    public GetAccountBillService(AccountBillRepository accountBillRepository) {
        this.accountBillRepository = accountBillRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.ACCOUNT_BILL_BY_ID, key = "#ownerEmail + ':' + #billId", unless = "#result == null")
    public Optional<AccountBill> execute(String ownerEmail, Long billId) {
        return accountBillRepository.findByIdAndOwnerEmail(billId, ownerEmail);
    }
}
