package br.com.everton.backendextrato.application.entrada.exception;

public class IncomeEntryNotFoundException extends IllegalArgumentException {

    public IncomeEntryNotFoundException(String message) {
        super(message);
    }
}
