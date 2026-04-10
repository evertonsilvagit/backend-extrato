package br.com.everton.backendextrato.application.categoriadivida.port.in;

import br.com.everton.backendextrato.application.categoriadivida.usecase.command.SaveDebtCategoryCommand;
import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;

public interface SaveDebtCategoryUseCase {
    DebtCategory execute(SaveDebtCategoryCommand command);
}
