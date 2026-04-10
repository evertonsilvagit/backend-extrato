package br.com.everton.backendextrato.application.conta.port.in;

public interface DeleteAccountBillUseCase {
    boolean execute(String ownerEmail, Long billId);
}
