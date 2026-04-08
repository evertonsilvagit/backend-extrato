package br.com.everton.backendextrato.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Column(name = "dias_recebimento")
    private String diasRecebimentoRaw;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "valor_liquido", nullable = false)
    private Boolean valorLiquido = Boolean.FALSE;

    @Column(name = "categoria_recebimento", nullable = false)
    private String categoriaRecebimento = "STANDARD";

    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntradaMes> meses = new ArrayList<>();

    @Column(name = "user_email")
    private String userEmail;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getTaxaImposto() { return taxaImposto; }
    public Integer getOrdem() { return ordem; }
    public Boolean getValorLiquido() { return valorLiquido; }
    public String getCategoriaRecebimento() { return categoriaRecebimento; }
    public List<EntradaMes> getMeses() { return meses; }
    public String getUserEmail() { return userEmail; }

    public void setNome(String nome) { this.nome = nome; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setTaxaImposto(BigDecimal taxaImposto) { this.taxaImposto = taxaImposto; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public void setValorLiquido(Boolean valorLiquido) { this.valorLiquido = valorLiquido; }
    public void setCategoriaRecebimento(String categoriaRecebimento) { this.categoriaRecebimento = categoriaRecebimento; }
    public void setMeses(List<EntradaMes> meses) { this.meses = meses; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    @Transient
    public List<Integer> getDiasRecebimento() {
        if (diasRecebimentoRaw == null || diasRecebimentoRaw.isEmpty()) {
            return new ArrayList<>();
        }

        String normalized = diasRecebimentoRaw
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
                .filter(value -> value != null && value >= 1 && value <= 31)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public void setDiasRecebimento(List<Integer> dias) {
        if (dias == null || dias.isEmpty()) {
            this.diasRecebimentoRaw = "";
        } else {
            this.diasRecebimentoRaw = dias.stream()
                    .distinct()
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }
}
