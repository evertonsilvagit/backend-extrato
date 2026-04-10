package br.com.everton.backendextrato.application.categoriaconta.exception;

public class AccountCategoryNotFoundException extends IllegalArgumentException {

    public AccountCategoryNotFoundException(String message) {
        super(message);
    }
}
