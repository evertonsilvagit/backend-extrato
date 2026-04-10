package br.com.everton.backendextrato.application.categoriaconta.port.in;

import br.com.everton.backendextrato.application.categoriaconta.usecase.command.SaveAccountCategoryCommand;
import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;

public interface SaveAccountCategoryUseCase {
    AccountCategory execute(SaveAccountCategoryCommand command);
}
