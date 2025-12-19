package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;
import java.util.List;

public record ContaDto(
        Long id,
        String descricao,
        BigDecimal valor,
        Integer diaPagamento,
        String categoria,
        List<Integer> mesesVigencia
) {}
