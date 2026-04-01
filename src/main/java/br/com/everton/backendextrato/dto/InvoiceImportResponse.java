package br.com.everton.backendextrato.dto;

public record InvoiceImportResponse(
        int created,
        int updated,
        int total
) {}
