package br.com.everton.backendextrato.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entrada")
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "taxa_imposto", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxaImposto;

    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntradaMes> meses = new ArrayList<>();

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getTaxaImposto() { return taxaImposto; }
    public List<EntradaMes> getMeses() { return meses; }

    public void setNome(String nome) { this.nome = nome; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setTaxaImposto(BigDecimal taxaImposto) { this.taxaImposto = taxaImposto; }
    public void setMeses(List<EntradaMes> meses) { this.meses = meses; }
}
