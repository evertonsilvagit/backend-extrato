package br.com.everton.backendextrato.dto;

import java.util.List;

public record FinancialSeparationWorkspaceDto(
        List<FinancialSeparationCashboxDto> cashboxes,
        List<FinancialSeparationEntryDto> entries
) {}
