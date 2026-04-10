package br.com.everton.backendextrato.application.entrada.exception;

public class IncomeEntryValidationException extends IllegalArgumentException {

    public IncomeEntryValidationException(String message) {
        super(message);
    }
}
