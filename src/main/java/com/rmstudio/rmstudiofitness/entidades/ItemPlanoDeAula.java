package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "item_plano_de_aula")
public class ItemPlanoDeAula implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_de_aula_id", nullable = false)
    private PlanoDeAula planoDeAula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercicio_id", nullable = false)
    private Exercicio exercicio;

    @Column(name = "dia_semana", nullable = false)
    private String diaSemana;

    @Column(name = "series")
    private Integer series;

    @Column(name = "repeticoes", length = 50)
    private String repeticoes;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlanoDeAula getPlanoDeAula() {
        return planoDeAula;
    }

    public void setPlanoDeAula(PlanoDeAula planoDeAula) {
        this.planoDeAula = planoDeAula;
    }

    public Exercicio getExercicio() {
        return exercicio;
    }

    public void setExercicio(Exercicio exercicio) {
        this.exercicio = exercicio;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public String getRepeticoes() {
        return repeticoes;
    }

    public void setRepeticoes(String repeticoes) {
        this.repeticoes = repeticoes;
    }
}
