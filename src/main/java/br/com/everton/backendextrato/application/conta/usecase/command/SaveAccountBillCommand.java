package br.com.everton.backendextrato.application.conta.usecase.command;

import java.math.BigDecimal;
import java.util.List;

public record SaveAccountBillCommand(
        Long id,
        String description,
        BigDecimal amount,
        Integer paymentDay,
        String categoryName,
        List<Integer> activeMonths,
        Integer sortOrder,
        String ownerEmail
) {}
