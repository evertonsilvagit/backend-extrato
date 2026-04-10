package br.com.everton.backendextrato.application.conta.usecase;

import br.com.everton.backendextrato.application.conta.port.in.ListAccountBillsUseCase;
import br.com.everton.backendextrato.application.conta.port.out.AccountBillRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.conta.AccountBill;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAccountBillsService implements ListAccountBillsUseCase {

    private final AccountBillRepository accountBillRepository;

    public ListAccountBillsService(AccountBillRepository accountBillRepository) {
        this.accountBillRepository = accountBillRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.ACCOUNT_BILLS, key = "#ownerEmail", unless = "#result == null || #result.isEmpty()")
    public List<AccountBill> execute(String ownerEmail) {
        return accountBillRepository.findAllByOwnerEmail(ownerEmail);
    }
}
