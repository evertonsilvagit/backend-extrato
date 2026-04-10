package br.com.everton.backendextrato.application.categoriadivida.exception;

public class DebtCategoryNotFoundException extends IllegalArgumentException {

    public DebtCategoryNotFoundException(String message) {
        super(message);
    }
}
