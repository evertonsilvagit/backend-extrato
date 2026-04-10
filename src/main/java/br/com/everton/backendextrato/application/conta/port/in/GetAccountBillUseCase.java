package br.com.everton.backendextrato.application.conta.port.in;

import br.com.everton.backendextrato.domain.conta.AccountBill;

import java.util.Optional;

public interface GetAccountBillUseCase {
    Optional<AccountBill> execute(String ownerEmail, Long billId);
}
