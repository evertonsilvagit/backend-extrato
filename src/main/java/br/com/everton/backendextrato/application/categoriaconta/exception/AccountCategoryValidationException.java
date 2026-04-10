package br.com.everton.backendextrato.application.categoriaconta.exception;

public class AccountCategoryValidationException extends IllegalArgumentException {

    public AccountCategoryValidationException(String message) {
        super(message);
    }
}
