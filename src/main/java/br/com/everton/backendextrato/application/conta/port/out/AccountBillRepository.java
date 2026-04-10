package br.com.everton.backendextrato.application.conta.port.out;

import br.com.everton.backendextrato.domain.conta.AccountBill;

import java.util.List;
import java.util.Optional;

public interface AccountBillRepository {
    Optional<AccountBill> findByIdAndOwnerEmail(Long id, String ownerEmail);
    List<AccountBill> findAllByOwnerEmail(String ownerEmail);
    List<AccountBill> findAllByOwnerEmailAndCategoryId(String ownerEmail, Long categoryId);
    AccountBill save(AccountBill bill);
    void saveAll(List<AccountBill> bills);
    void delete(AccountBill bill);
}
