package br.com.everton.backendextrato.application.categoriaconta.port.out;

public interface AccountCategoryUsageRepository {
    boolean isInUse(String ownerEmail, Long categoryId);
}
