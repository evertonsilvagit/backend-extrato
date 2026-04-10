package br.com.everton.backendextrato.application.divida.port.in;

import br.com.everton.backendextrato.domain.divida.Debt;

import java.util.List;

public interface ListDebtsUseCase {
    List<Debt> execute(String ownerEmail);
}
