package br.com.everton.backendextrato.application.conta.port.in;

import br.com.everton.backendextrato.application.conta.usecase.command.SaveAccountBillCommand;
import br.com.everton.backendextrato.domain.conta.AccountBill;

public interface SaveAccountBillUseCase {
    AccountBill execute(SaveAccountBillCommand command);
}
