package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import java.util.Objects;

@Entity
@Table(name = "perfil")
public class Perfil implements GrantedAuthority { // <--- PONTO 1: "implements GrantedAuthority"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 50)
    private String nome;

    // Getters e Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public String getAuthority() { // <--- PONTO 2: Método obrigatório
        return this.nome;
    }

    // Opcional, mas recomendado
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Perfil perfil = (Perfil) o;
        return Objects.equals(nome, perfil.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}
