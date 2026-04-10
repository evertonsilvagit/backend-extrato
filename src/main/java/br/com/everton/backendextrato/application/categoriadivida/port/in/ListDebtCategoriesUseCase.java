package br.com.everton.backendextrato.application.categoriadivida.port.in;

import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;

import java.util.List;

public interface ListDebtCategoriesUseCase {
    List<DebtCategory> execute(String ownerEmail);
}
