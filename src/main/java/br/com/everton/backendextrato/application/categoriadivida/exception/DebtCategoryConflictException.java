package br.com.everton.backendextrato.application.categoriadivida.exception;

public class DebtCategoryConflictException extends IllegalStateException {

    public DebtCategoryConflictException(String message) {
        super(message);
    }
}
