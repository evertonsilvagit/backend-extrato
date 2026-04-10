package br.com.everton.backendextrato.application.categoriaconta.port.in;

public interface DeleteAccountCategoryUseCase {
    boolean execute(String ownerEmail, Long categoryId);
}
