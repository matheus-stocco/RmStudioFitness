package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "cidade")
public class Cidade implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", referencedColumnName = "id", nullable = false)
    private Estado estado;

    public Cidade() {}

    public Cidade(String nome, Estado estado) {
        this.nome = nome;
        this.estado = estado;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    // Utilitários
    public String getNomeCompleto() {
        return nome + (estado != null ? " - " + estado.getUf() : "");
    }

    public String getNomeComEstado() {
        return nome + (estado != null ? " (" + estado.getUf() + ")" : "");
    }

    public boolean isValid() {
        return nome != null && !nome.trim().isEmpty() && estado != null;
    }

    public String getNomeEstado() {
        return estado != null ? estado.getNome() : "";
    }

    public String getUfEstado() {
        return estado != null ? estado.getUf() : "";
    }

    // equals / hashCode / toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cidade)) return false;
        Cidade that = (Cidade) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return nome + (estado != null ? " (" + estado.getUf() + ")" : "");
    }
}
