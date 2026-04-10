package br.com.everton.backendextrato.application.categoriadivida.port.in;

public interface DeleteDebtCategoryUseCase {
    boolean execute(String ownerEmail, Long categoryId);
}
