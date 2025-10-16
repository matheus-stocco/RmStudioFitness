package com.rmstudio.rmstudiofitness.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "item_plano_aula")
public class ItemPlanoAula implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muitos itens pertencem a um plano
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_aula_id", nullable = false)
    @JsonIgnoreProperties({"itens", "aluno"})
    private PlanoAula planoAula;

    // Dia da semana
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DiaSemana dia;

    // Exercício do item
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "exercicio_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exercicio exercicio;

    @Column(nullable = false)
    private Integer series;

    @Column(nullable = false, length = 50)
    private String repeticoes;

    @Column(name = "tempo_descanso_segundos")
    private Integer tempoDescansoSegundos;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "ordem")
    private Integer ordem = 0;

    // ─── Construtores ───────────────────────────────────────────────────────────

    public ItemPlanoAula() {}

    public ItemPlanoAula(DiaSemana dia, Exercicio exercicio, int series, int repeticoes) {
        this.dia = dia;
        this.exercicio = exercicio;
        this.series = series;
        this.repeticoes = String.valueOf(repeticoes);
    }

    public ItemPlanoAula(PlanoAula planoAula, DiaSemana dia, Exercicio exercicio, int series, int repeticoes) {
        this.planoAula = planoAula;
        this.dia = dia;
        this.exercicio = exercicio;
        this.series = series;
        this.repeticoes = String.valueOf(repeticoes);
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlanoAula getPlanoAula() { return planoAula; }
    public void setPlanoAula(PlanoAula planoAula) { this.planoAula = planoAula; }

    public DiaSemana getDia() { return dia; }
    public void setDia(DiaSemana dia) { this.dia = dia; }

    public Exercicio getExercicio() { return exercicio; }
    public void setExercicio(Exercicio exercicio) { this.exercicio = exercicio; }

    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }

    public String getRepeticoes() { return repeticoes; }
    public void setRepeticoes(String repeticoes) { this.repeticoes = repeticoes; }

    public Integer getTempoDescansoSegundos() { return tempoDescansoSegundos; }
    public void setTempoDescansoSegundos(Integer tempoDescansoSegundos) { this.tempoDescansoSegundos = tempoDescansoSegundos; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    // ─── Métodos auxiliares ────────────────────────────────────────────────────

    @Transient
    public String getDiaLabel() {
        return dia != null ? dia.getLabel() : "";
    }

    @Transient
    public String getExercicioNome() {
        return (exercicio != null && exercicio.getNome() != null) ? exercicio.getNome() : "—";
    }

    @Transient
    public String getPlanoNome() {
        return (planoAula != null && planoAula.getNome() != null) ? planoAula.getNome() : "—";
    }

    @Transient
    public boolean isValid() {
        return dia != null
            && exercicio != null
            && series != null && series > 0
            && repeticoes != null && !repeticoes.trim().isEmpty();
    }

    @Transient
    public Integer getVolumeTotal() {
        // Não é mais possível calcular o volume se repetições for texto
        return 0;
    }

    @Transient
    public Double getVolumeComCarga() {
        // Não é mais possível calcular o volume se repetições for texto
        return 0.0;
    }

    @Transient
    public String getInfoCompleta() {
        StringBuilder info = new StringBuilder();
        info.append(getExercicioNome()).append(" - ")
            .append(series).append("x").append(repeticoes);

        if (tempoDescansoSegundos != null && tempoDescansoSegundos > 0) {
            info.append(" - Descanso: ").append(formatarTempoDescanso());
        }
        return info.toString();
    }

    @Transient
    public String formatarTempoDescanso() {
        if (tempoDescansoSegundos == null || tempoDescansoSegundos <= 0) return "";
        int minutos = tempoDescansoSegundos / 60;
        int segundos = tempoDescansoSegundos % 60;
        return (minutos > 0) ? String.format("%dm%ds", minutos, segundos)
                             : String.format("%ds", segundos);
    }

    @Transient
    public boolean isAltaIntensidade() {
        return (series != null && series >= 4);
            // Não é mais possível calcular intensidade por repetições
    }

    public void copiarDe(ItemPlanoAula outro) {
        if (outro != null) {
            this.dia = outro.dia;
            this.exercicio = outro.exercicio;
            this.series = outro.series;
            this.repeticoes = outro.repeticoes;
            this.tempoDescansoSegundos = outro.tempoDescansoSegundos;
            this.observacoes = outro.observacoes;
        }
    }

    // ─── equals, hashCode, toString ────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("%s: %s %dx%s",
            getDiaLabel(),
            getExercicioNome(),
            series != null ? series : 0,
            repeticoes != null ? repeticoes : "0"
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPlanoAula)) return false;
        ItemPlanoAula that = (ItemPlanoAula) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
