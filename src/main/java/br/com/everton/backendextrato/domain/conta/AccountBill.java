package br.com.everton.backendextrato.domain.conta;

import java.math.BigDecimal;
import java.util.List;

public record AccountBill(
        Long id,
        String description,
        BigDecimal amount,
        Integer paymentDay,
        Long categoryId,
        String categoryName,
        List<Integer> activeMonths,
        String ownerEmail,
        Integer sortOrder
) {}
