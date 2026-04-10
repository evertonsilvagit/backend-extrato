package br.com.everton.backendextrato.application.categoriaconta.exception;

public class AccountCategoryConflictException extends IllegalStateException {

    public AccountCategoryConflictException(String message) {
        super(message);
    }
}
