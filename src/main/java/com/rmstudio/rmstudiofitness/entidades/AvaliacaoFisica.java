package com.rmstudio.rmstudiofitness.entidades;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(name = "massa_muscular", precision = 10, scale = 2)
    private BigDecimal massaMuscular;

    @Column(name = "hidratacao", precision = 10, scale = 2)
    private BigDecimal hidratacao;

    @Column(name = "densidade_ossea", precision = 10, scale = 2)
    private BigDecimal densidadeOssea;

    @Column(name = "taxa_metabolismo_basal", precision = 10, scale = 2)
    private BigDecimal taxaMetabolismoBasal;
    
    @Column(name = "gordura_visceral", precision = 10, scale = 2)
    private BigDecimal gorduraVisceral;

    // Medidas Corporais (em cm)
    @Column(name = "medida_pescoco", precision = 10, scale = 2)
    private BigDecimal medidaPescoco;

    @Column(name = "medida_cintura", precision = 10, scale = 2)
    private BigDecimal medidaCintura;

    @Column(name = "medida_quadril", precision = 10, scale = 2)
    private BigDecimal medidaQuadril;

    @Column(name = "medida_braco_direito", precision = 10, scale = 2)
    private BigDecimal medidaBracoDireito;

    @Column(name = "medida_braco_esquerdo", precision = 10, scale = 2)
    private BigDecimal medidaBracoEsquerdo;

    @Column(name = "medida_perna_direita", precision = 10, scale = 2)
    private BigDecimal medidaPernaDireita;

    @Column(name = "medida_perna_esquerda", precision = 10, scale = 2)
    private BigDecimal medidaPernaEsquerda;

    @Column(length = 500)
    private String observacoes;

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

    public BigDecimal getMassaMuscular() { return massaMuscular; }
    public void setMassaMuscular(BigDecimal massaMuscular) { this.massaMuscular = massaMuscular; }

    public BigDecimal getHidratacao() { return hidratacao; }
    public void setHidratacao(BigDecimal hidratacao) { this.hidratacao = hidratacao; }

    public BigDecimal getDensidadeOssea() { return densidadeOssea; }
    public void setDensidadeOssea(BigDecimal densidadeOssea) { this.densidadeOssea = densidadeOssea; }

    public BigDecimal getTaxaMetabolismoBasal() { return taxaMetabolismoBasal; }
    public void setTaxaMetabolismoBasal(BigDecimal taxaMetabolismoBasal) { this.taxaMetabolismoBasal = taxaMetabolismoBasal; }

    public BigDecimal getGorduraVisceral() { return gorduraVisceral; }
    public void setGorduraVisceral(BigDecimal gorduraVisceral) { this.gorduraVisceral = gorduraVisceral; }

    public BigDecimal getMedidaPescoco() { return medidaPescoco; }
    public void setMedidaPescoco(BigDecimal medidaPescoco) { this.medidaPescoco = medidaPescoco; }

    public BigDecimal getMedidaCintura() { return medidaCintura; }
    public void setMedidaCintura(BigDecimal medidaCintura) { this.medidaCintura = medidaCintura; }

    public BigDecimal getMedidaQuadril() { return medidaQuadril; }
    public void setMedidaQuadril(BigDecimal medidaQuadril) { this.medidaQuadril = medidaQuadril; }

    public BigDecimal getMedidaBracoDireito() { return medidaBracoDireito; }
    public void setMedidaBracoDireito(BigDecimal medidaBracoDireito) { this.medidaBracoDireito = medidaBracoDireito; }

    public BigDecimal getMedidaBracoEsquerdo() { return medidaBracoEsquerdo; }
    public void setMedidaBracoEsquerdo(BigDecimal medidaBracoEsquerdo) { this.medidaBracoEsquerdo = medidaBracoEsquerdo; }

    public BigDecimal getMedidaPernaDireita() { return medidaPernaDireita; }
    public void setMedidaPernaDireita(BigDecimal medidaPernaDireita) { this.medidaPernaDireita = medidaPernaDireita; }

    public BigDecimal getMedidaPernaEsquerda() { return medidaPernaEsquerda; }
    public void setMedidaPernaEsquerda(BigDecimal medidaPernaEsquerda) { this.medidaPernaEsquerda = medidaPernaEsquerda; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Pessoa getPessoa() { return pessoa; }
    public void setPessoa(Pessoa pessoa) { this.pessoa = pessoa; }

    // Utilitários
    /** Calcula o IMC = peso / (altura^2). */
    @Transient // Garante que o JPA não tente mapear este método como uma coluna no banco
    @JsonProperty("imc")
    public BigDecimal getImc() {
        if (peso != null && altura != null && altura.compareTo(BigDecimal.ZERO) > 0) {
            return peso.divide(altura.multiply(altura), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /** Classificação textual do IMC. */
    @Transient
    @JsonProperty("classificacaoImc")
    public String getClassificacaoImc() {
        BigDecimal imc = getImc();
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
