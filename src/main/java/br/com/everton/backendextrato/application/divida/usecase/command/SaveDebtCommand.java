package br.com.everton.backendextrato.application.divida.usecase.command;

import java.math.BigDecimal;

public record SaveDebtCommand(
        Long id,
        String description,
        BigDecimal amount,
        String categoryName,
        Integer sortOrder,
        String ownerEmail
) {}
