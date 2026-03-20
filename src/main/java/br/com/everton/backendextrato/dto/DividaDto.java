package br.com.everton.backendextrato.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record DividaDto(
        Long id,

        @JsonProperty("description")
        String descricao,

        @JsonProperty("amount")
        BigDecimal valor,

        @JsonProperty("group")
        String grupo
) {}
