package br.com.everton.backendextrato.dto;

public record FinancialSeparationCashboxDto(
        String id,
        String name,
        String owner,
        String instrument,
        boolean locked
) {}
