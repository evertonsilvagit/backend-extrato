package br.com.everton.backendextrato.application.categoriaconta.usecase.command;

public record SaveAccountCategoryCommand(
        Long id,
        String name,
        String ownerEmail
) {}
