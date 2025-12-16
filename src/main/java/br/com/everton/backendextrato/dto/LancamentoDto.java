package br.com.everton.backendextrato.dto;

import br.com.everton.backendextrato.model.Tipo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoDto(
        Long id,
        LocalDate data,
        Tipo tipo,
        BigDecimal valor,
        String descricao,
        String categoria,
        Long contaId
) {}
