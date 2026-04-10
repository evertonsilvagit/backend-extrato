package br.com.everton.backendextrato.application.conta.port.in;

import br.com.everton.backendextrato.domain.conta.AccountBill;

import java.util.List;

public interface ListAccountBillsUseCase {
    List<AccountBill> execute(String ownerEmail);
}
