package com.rmstudio.rmstudiofitness.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "tipo_plano")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class TipoPlano implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 255)
    @Column(length = 255)
    private String descricao;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_modificacao")
    private LocalDateTime dataModificacao;

    @Transient
    private List<String> beneficios;

    @Positive
    @Column(name = "limite_avaliacoes")
    private Integer limiteAvaliacoes;

    @Column(name = "acesso_planos_aula")
    private Boolean acessoPlanosAula = true;

    // Construtores
    public TipoPlano() {
        this.dataCriacao = LocalDateTime.now();
        this.ativo = true;
        this.acessoPlanosAula = true;
    }

    public TipoPlano(String nome, BigDecimal valor) {
        this();
        this.nome = nome;
        this.valor = valor;
    }

    public TipoPlano(String nome, String descricao, BigDecimal valor) {
        this();
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCriacao == null) this.dataCriacao = LocalDateTime.now();
        if (this.ativo == null) this.ativo = true;
        if (this.acessoPlanosAula == null) this.acessoPlanosAula = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.dataModificacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataModificacao() { return dataModificacao; }
    public void setDataModificacao(LocalDateTime dataModificacao) { this.dataModificacao = dataModificacao; }

    public List<String> getBeneficios() { return beneficios; }
    public void setBeneficios(List<String> beneficios) { this.beneficios = beneficios; }

    public Integer getLimiteAvaliacoes() { return limiteAvaliacoes; }
    public void setLimiteAvaliacoes(Integer limiteAvaliacoes) { this.limiteAvaliacoes = limiteAvaliacoes; }

    public Boolean getAcessoPlanosAula() { return acessoPlanosAula; }
    public void setAcessoPlanosAula(Boolean acessoPlanosAula) { this.acessoPlanosAula = acessoPlanosAula; }

    // Utilitários (transientes)
    @Transient
    public String getValorFormatado() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(valor != null ? valor : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getValorMensal() {
        if (valor == null) return BigDecimal.ZERO;
        return valor.divide(BigDecimal.valueOf(1), 2, RoundingMode.HALF_UP); // Valor mensal é o próprio valor
    }

    @Transient
    public String getValorMensalFormatado() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        BigDecimal vm = getValorMensal();
        return nf.format(vm != null ? vm : BigDecimal.ZERO);
    }

    @Transient
    public boolean isValid() {
        return nome != null && !nome.trim().isEmpty()
            && valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    @Transient
    public boolean isAtivo() { return Boolean.TRUE.equals(ativo); }

    @Transient
    public boolean temAcessoPlanosAula() { return Boolean.TRUE.equals(acessoPlanosAula); }

    @Transient
    public boolean temLimiteAvaliacoes() { return limiteAvaliacoes != null && limiteAvaliacoes > 0; }

    @Transient
    public String getInfoCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(nome);
        sb.append(" - ").append(getValorFormatado());
        return sb.toString();
    }

    @Transient
    public BigDecimal calcularDesconto(TipoPlano outroPlano) {
        if (outroPlano == null || outroPlano.getValor() == null || valor == null) return BigDecimal.ZERO;
        BigDecimal vmEste = getValorMensal();
        BigDecimal vmOutro = outroPlano.getValorMensal();
        if (vmOutro == null || vmOutro.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal dif = vmOutro.subtract(vmEste != null ? vmEste : BigDecimal.ZERO);
        return dif.divide(vmOutro, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public TipoPlano duplicar(String novoNome) {
        TipoPlano n = new TipoPlano();
        n.setNome(novoNome);
        n.setDescricao(this.descricao);
        n.setValor(this.valor);
        n.setAtivo(this.ativo);
        n.setAcessoPlanosAula(this.acessoPlanosAula);
        return n;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoPlano)) return false;
        TipoPlano that = (TipoPlano) o;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return nome + " - " + getValorFormatado();
    }
}
