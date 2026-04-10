package br.com.everton.backendextrato.application.conta.exception;

public class AccountBillValidationException extends IllegalArgumentException {

    public AccountBillValidationException(String message) {
        super(message);
    }
}
