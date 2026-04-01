package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;

public record InvoiceRecordDto(
        String sourcePath,
        String filename,
        String year,
        String monthFolder,
        String issueDate,
        String number,
        String customerDocument,
        String customerName,
        String customerEmail,
        String customerCity,
        BigDecimal grossAmount,
        BigDecimal issAmount,
        BigDecimal netAmount,
        String serviceType,
        String notes,
        Boolean canceled,
        String relativePath
) {}
