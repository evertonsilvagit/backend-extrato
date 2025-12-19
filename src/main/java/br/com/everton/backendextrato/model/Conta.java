package br.com.everton.backendextrato.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "dia_pagamento", nullable = false)
    private Integer diaPagamento;

    @Column(name = "meses_vigencia")
    private String mesesVigenciaRaw;

    @Column
    private String categoria;

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public Integer getDiaPagamento() { return diaPagamento; }
    public String getCategoria() { return categoria; }

    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setDiaPagamento(Integer diaPagamento) { this.diaPagamento = diaPagamento; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Transient
    public List<Integer> getMesesVigencia() {
        if (mesesVigenciaRaw == null || mesesVigenciaRaw.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(mesesVigenciaRaw.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public void setMesesVigencia(List<Integer> meses) {
        if (meses == null || meses.isEmpty()) {
            this.mesesVigenciaRaw = "";
        } else {
            this.mesesVigenciaRaw = meses.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }
}
