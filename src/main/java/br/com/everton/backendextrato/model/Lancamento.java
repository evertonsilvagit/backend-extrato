package br.com.everton.backendextrato.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamento")
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(length = 500)
    private String descricao;

    @Column(length = 100)
    private String categoria;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    public Long getId() { return id; }
    public LocalDate getData() { return data; }
    public Tipo getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public String getCategoria() { return categoria; }
    public Long getContaId() { return contaId; }

    public void setData(LocalDate data) { this.data = data; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setContaId(Long contaId) { this.contaId = contaId; }
}
