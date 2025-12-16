package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLancamentoRequest(
        LocalDate data,
        String tipo,
        BigDecimal valor,
        String descricao,
        String categoria,
        Long contaId
) {}
