package br.com.everton.backendextrato.application.entrada.port.in;

public interface DeleteIncomeEntryUseCase {
    boolean execute(String ownerEmail, Long incomeEntryId);
}
