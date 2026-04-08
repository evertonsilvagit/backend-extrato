package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateEntradaRequest(
        Long id,
        String nome,
        String tipo,
        BigDecimal valor,
        BigDecimal taxaImposto,
        List<Integer> diasRecebimento,
        Boolean valorLiquido,
        String categoriaRecebimento,
        List<Integer> mesesVigencia,
        Integer ordem
) {}
