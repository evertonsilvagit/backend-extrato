package br.com.everton.backendextrato.application.categoriadivida.usecase.command;

public record SaveDebtCategoryCommand(
        Long id,
        String name,
        String ownerEmail
) {}
