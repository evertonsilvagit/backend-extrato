package br.com.everton.backendextrato.application.divida.port.in;

public interface DeleteDebtUseCase {
    boolean execute(String ownerEmail, Long debtId);
}
