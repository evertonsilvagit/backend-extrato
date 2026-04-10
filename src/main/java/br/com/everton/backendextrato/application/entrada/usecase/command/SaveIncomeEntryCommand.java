package br.com.everton.backendextrato.application.entrada.usecase.command;

import java.math.BigDecimal;
import java.util.List;

public record SaveIncomeEntryCommand(
        Long id,
        String name,
        String type,
        BigDecimal amount,
        BigDecimal taxRate,
        List<Integer> paymentDays,
        Boolean netAmount,
        String incomeCategory,
        List<Integer> activeMonths,
        Integer sortOrder,
        String ownerEmail
) {}
