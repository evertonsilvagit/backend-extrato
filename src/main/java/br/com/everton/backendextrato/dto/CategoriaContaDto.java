package br.com.everton.backendextrato.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoriaContaDto(
        Long id,

        @JsonProperty("name")
        String nome
) {}
