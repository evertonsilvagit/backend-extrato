package br.com.everton.backendextrato.model;

import jakarta.persistence.*;

@Entity
@Table(name = "entrada_mes")
public class EntradaMes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id", nullable = false)
    private Entrada entrada;

    @Column(nullable = false)
    private Integer mes; // 1..12

    public Long getId() { return id; }
    public Entrada getEntrada() { return entrada; }
    public Integer getMes() { return mes; }

    public void setEntrada(Entrada entrada) { this.entrada = entrada; }
    public void setMes(Integer mes) { this.mes = mes; }
}
