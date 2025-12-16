package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateEntradaRequest(
        String nome,
        String tipo,
        BigDecimal valor,
        BigDecimal taxaImposto,
        List<Integer> mesesVigencia
) {}
