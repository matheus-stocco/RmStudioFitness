package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "exercicio")
public class Exercicio implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "grupo_muscular", nullable = false, length = 40)
    private String grupoMuscular;

    // Construtores
    public Exercicio() {}

    public Exercicio(String nome, String grupoMuscular) {
        this.nome = nome;
        this.grupoMuscular = grupoMuscular;
    }

    public Exercicio(String nome, String descricao, String grupoMuscular) {
        this.nome = nome;
        this.descricao = descricao;
        this.grupoMuscular = grupoMuscular;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }

    // Métodos utilitários
    public String getInfoCompleta() {
        return nome + " (" + grupoMuscular + ")";
    }

    public boolean isValid() {
        return nome != null && !nome.trim().isEmpty() &&
               grupoMuscular != null && !grupoMuscular.trim().isEmpty();
    }

    public void normalizarNome() {
        if (nome != null && !nome.trim().isEmpty()) {
            String[] palavras = nome.trim().toLowerCase().split("\\s+");
            StringBuilder nomeNormalizado = new StringBuilder();
            for (String palavra : palavras) {
                if (!palavra.isEmpty()) {
                    if (nomeNormalizado.length() > 0) nomeNormalizado.append(" ");
                    nomeNormalizado.append(palavra.substring(0, 1).toUpperCase())
                                   .append(palavra.substring(1));
                }
            }
            this.nome = nomeNormalizado.toString();
        }
    }

    public void normalizarGrupoMuscular() {
        if (grupoMuscular != null && !grupoMuscular.trim().isEmpty()) {
            String normalizado = grupoMuscular.trim().toLowerCase();
            this.grupoMuscular = normalizado.substring(0, 1).toUpperCase() + normalizado.substring(1);
        }
    }

    public void normalizar() {
        normalizarNome();
        normalizarGrupoMuscular();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Exercicio)) return false;
        Exercicio exercicio = (Exercicio) obj;
        return Objects.equals(id, exercicio.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return nome + " (" + grupoMuscular + ")";
    }
}
