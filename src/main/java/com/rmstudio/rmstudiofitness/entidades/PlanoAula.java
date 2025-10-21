package com.rmstudio.rmstudiofitness.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "plano_aula")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class PlanoAula implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 500)
    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_modificacao")
    private LocalDateTime dataModificacao;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aluno_id")
    private Pessoa aluno;

    @OneToMany(
        mappedBy = "planoAula",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<ItemPlanoAula> itens = new ArrayList<>();

    // Construtores
    public PlanoAula() {
        this.dataCriacao = LocalDateTime.now();
        this.ativo = true;
    }

    public PlanoAula(String nome) {
        this(); this.nome = nome;
    }

    public PlanoAula(String nome, String descricao) {
        this(); this.nome = nome; this.descricao = descricao;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCriacao == null) this.dataCriacao = LocalDateTime.now();
        if (this.ativo == null) this.ativo = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.dataModificacao = LocalDateTime.now();
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataModificacao() { return dataModificacao; }
    public void setDataModificacao(LocalDateTime dataModificacao) { this.dataModificacao = dataModificacao; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public List<ItemPlanoAula> getItens() {
        if (itens == null) itens = new ArrayList<>();
        return itens;
    }
    public void setItens(List<ItemPlanoAula> itens) {
        if (this.itens != null) {
            this.itens.forEach(i -> i.setPlanoAula(null));
            this.itens.clear();
        } else {
            this.itens = new ArrayList<>();
        }
        if (itens != null) itens.forEach(this::addItem);
    }

    // Auxiliares bidirecionais
    public void addItem(ItemPlanoAula item) {
        if (item != null) {
            if (item.getPlanoAula() != null && item.getPlanoAula() != this) {
                item.getPlanoAula().removeItem(item);
            }
            item.setPlanoAula(this);
            getItens().add(item);
        }
    }

    public void removeItem(ItemPlanoAula item) {
        if (item != null && getItens().contains(item)) {
            getItens().remove(item);
            item.setPlanoAula(null);
        }
    }

    // Computados/Transientes
    @Transient public boolean hasItens() { return getItens() != null && !getItens().isEmpty(); }
    @Transient public int getQuantidadeItens() { return getItens().size(); }
    @Transient public List<ItemPlanoAula> getItensPorDia(DiaSemana dia) {
        return getItens().stream().filter(i -> i.getDia() == dia).collect(Collectors.toList());
    }
    @Transient public int getQuantidadeDiasAtivos() {
        return (int) getItens().stream().map(ItemPlanoAula::getDia).distinct().count();
    }
    @Transient public Integer getVolumeTotal() {
        return getItens().stream().mapToInt(ItemPlanoAula::getVolumeTotal).sum();
    }
    @Transient public boolean isValid() { return nome != null && !nome.trim().isEmpty() && hasItens(); }
    @Transient public boolean isAtivo() { return ativo != null && ativo; }

    @Transient
    public Map<String, List<ItemPlanoAula>> getItensAgrupadosPorDia() {
        return getItens().stream()
                .collect(Collectors.groupingBy(item -> item.getDia().getLabel()));
    }

    @Transient public List<Exercicio> getExerciciosUnicos() {
        return getItens().stream().map(ItemPlanoAula::getExercicio).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }
    @Transient public List<String> getGruposMusculares() {
        return getItens().stream()
                .map(ItemPlanoAula::getExercicio)
                .filter(Objects::nonNull)
                .map(Exercicio::getGrupoMuscular)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public Pessoa getAluno() {
        return aluno;
    }

    public void setAluno(Pessoa aluno) {
        this.aluno = aluno;
    }

    public PlanoAula duplicar(String novoNome) {
        PlanoAula n = new PlanoAula();
        n.setNome(novoNome);
        n.setDescricao(this.descricao);
        for (ItemPlanoAula it : this.getItens()) {
            ItemPlanoAula novo = new ItemPlanoAula();
            novo.copiarDe(it);
            n.addItem(novo);
        }
        return n;
    }
    @Transient public String getResumo() {
        StringBuilder r = new StringBuilder();
        r.append(nome);
        if (hasItens()) {
            r.append(" (").append(getQuantidadeItens()).append(" exercícios, ")
             .append(getQuantidadeDiasAtivos()).append(" dias)");
        }
        return r.toString();
    }

    // equals/hashCode por id
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanoAula)) return false;
        PlanoAula that = (PlanoAula) o;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() { return nome != null ? nome : "Plano sem nome"; }
}
