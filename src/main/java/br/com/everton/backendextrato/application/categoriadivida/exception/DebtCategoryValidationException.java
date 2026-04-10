package br.com.everton.backendextrato.application.categoriadivida.exception;

public class DebtCategoryValidationException extends IllegalArgumentException {

    public DebtCategoryValidationException(String message) {
        super(message);
    }
}
