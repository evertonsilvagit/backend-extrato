package br.com.everton.backendextrato.domain.divida;

import java.math.BigDecimal;

public record Debt(
        Long id,
        String description,
        BigDecimal amount,
        Long categoryId,
        String categoryName,
        Integer sortOrder,
        String ownerEmail
) {}
