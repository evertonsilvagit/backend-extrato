package br.com.everton.backendextrato.application.categoriadivida.port.out;

public interface DebtCategoryUsageRepository {
    boolean isInUse(String ownerEmail, Long categoryId);
}
