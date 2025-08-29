package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "avaliacao_fisica")
public class AvaliacaoFisica implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDate dataAvaliacao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal peso;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal altura;

    @Column(name = "gordura_corporal", precision = 10, scale = 2)
    private BigDecimal gorduraCorporal;

    @Column(name = "massa_magra", precision = 10, scale = 2)
    private BigDecimal massaMagra;

    @Column(name = "agua_corporal", precision = 10, scale = 2)
    private BigDecimal aguaCorporal;

    @Column(name = "massa_ossea", precision = 10, scale = 2)
    private BigDecimal massaOssea;

    @Column(name = "taxa_basal", precision = 10, scale = 2)
    private BigDecimal taxaBasal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    public AvaliacaoFisica() {}

    public AvaliacaoFisica(Pessoa pessoa, BigDecimal peso, BigDecimal altura) {
        this.pessoa = pessoa;
        this.peso = peso;
        this.altura = altura;
        this.dataAvaliacao = LocalDate.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.dataAvaliacao == null) {
            this.dataAvaliacao = LocalDate.now();
        }
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDate dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getAltura() { return altura; }
    public void setAltura(BigDecimal altura) { this.altura = altura; }

    public BigDecimal getGorduraCorporal() { return gorduraCorporal; }
    public void setGorduraCorporal(BigDecimal gorduraCorporal) { this.gorduraCorporal = gorduraCorporal; }

    public BigDecimal getMassaMagra() { return massaMagra; }
    public void setMassaMagra(BigDecimal massaMagra) { this.massaMagra = massaMagra; }

    public BigDecimal getAguaCorporal() { return aguaCorporal; }
    public void setAguaCorporal(BigDecimal aguaCorporal) { this.aguaCorporal = aguaCorporal; }

    public BigDecimal getMassaOssea() { return massaOssea; }
    public void setMassaOssea(BigDecimal massaOssea) { this.massaOssea = massaOssea; }

    public BigDecimal getTaxaBasal() { return taxaBasal; }
    public void setTaxaBasal(BigDecimal taxaBasal) { this.taxaBasal = taxaBasal; }

    public Pessoa getPessoa() { return pessoa; }
    public void setPessoa(Pessoa pessoa) { this.pessoa = pessoa; }

    // Utilitários
    /** Calcula o IMC = peso / (altura^2). */
    public BigDecimal calcularIMC() {
        if (peso != null && altura != null && altura.compareTo(BigDecimal.ZERO) > 0) {
            return peso.divide(altura.multiply(altura), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /** Classificação textual do IMC. */
    public String classificarIMC() {
        BigDecimal imc = calcularIMC();
        if (imc == null) return "N/A";

        if (imc.compareTo(new BigDecimal("18.5")) < 0) return "Abaixo do peso";
        else if (imc.compareTo(new BigDecimal("25")) < 0) return "Peso normal";
        else if (imc.compareTo(new BigDecimal("30")) < 0) return "Sobrepeso";
        else if (imc.compareTo(new BigDecimal("35")) < 0) return "Obesidade grau I";
        else if (imc.compareTo(new BigDecimal("40")) < 0) return "Obesidade grau II";
        else return "Obesidade grau III";
    }

    /** Valida campos obrigatórios. */
    public boolean isValid() {
        return pessoa != null && peso != null && altura != null && dataAvaliacao != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AvaliacaoFisica)) return false;
        AvaliacaoFisica that = (AvaliacaoFisica) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "AvaliacaoFisica{" +
                "id=" + id +
                ", dataAvaliacao=" + dataAvaliacao +
                ", peso=" + peso +
                ", altura=" + altura +
                ", pessoa=" + (pessoa != null ? pessoa.getNome() : "null") +
                '}';
    }
}
