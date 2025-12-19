package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;

public record DividaDto(
        Long id,
        String descricao,
        BigDecimal valor,
        String grupo
) {}
