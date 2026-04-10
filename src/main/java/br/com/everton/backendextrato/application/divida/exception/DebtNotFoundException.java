package br.com.everton.backendextrato.application.divida.exception;

public class DebtNotFoundException extends IllegalArgumentException {

    public DebtNotFoundException(String message) {
        super(message);
    }
}
