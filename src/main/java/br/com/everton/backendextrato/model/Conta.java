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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_conta_id", nullable = false)
    private CategoriaConta categoria;

    @Column(name = "user_email")
    private String userEmail;

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public Integer getDiaPagamento() { return diaPagamento; }
    public CategoriaConta getCategoria() { return categoria; }
    public String getUserEmail() { return userEmail; }

    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setDiaPagamento(Integer diaPagamento) { this.diaPagamento = diaPagamento; }
    public void setCategoria(CategoriaConta categoria) { this.categoria = categoria; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    @Transient
    public List<Integer> getMesesVigencia() {
        if (mesesVigenciaRaw == null || mesesVigenciaRaw.isEmpty()) {
            return new ArrayList<>();
        }

        String normalized = mesesVigenciaRaw
                .replace("[", "")
                .replace("]", "")
                .trim();

        if (normalized.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(value -> value != null && value >= 1 && value <= 12)
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
