package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialSeparationEntryDto(
        String id,
        String date,
        String description,
        BigDecimal amount,
        String cashboxId,
        String status,
        String categoryGroup,
        String category,
        List<String> tags,
        String kind,
        String transferNature,
        String transferTargetCashboxId,
        boolean receiptAttached,
        String notes
) {}
