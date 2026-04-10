package br.com.everton.backendextrato.application.categoriaconta.port.in;

import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;

import java.util.List;

public interface ListAccountCategoriesUseCase {
    List<AccountCategory> execute(String ownerEmail);
}
