package br.com.everton.backendextrato.application.conta.exception;

public class AccountBillNotFoundException extends IllegalArgumentException {

    public AccountBillNotFoundException(String message) {
        super(message);
    }
}
