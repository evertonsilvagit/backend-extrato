package br.com.everton.backendextrato.application.divida.exception;

public class DebtValidationException extends IllegalArgumentException {

    public DebtValidationException(String message) {
        super(message);
    }
}
