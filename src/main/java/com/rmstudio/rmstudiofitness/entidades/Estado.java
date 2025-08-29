package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "estado")
public class Estado implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 2, unique = true)
    private String uf;

    public Estado() {}

    public Estado(String nome, String uf) {
        this.nome = nome;
        this.uf = uf != null ? uf.toUpperCase() : null;
    }

    // Normalizações automáticas (opcional)
    @PrePersist
    @PreUpdate
    private void normalize() {
        if (uf != null) uf = uf.trim().toUpperCase();
        if (nome != null) {
            String[] p = nome.trim().toLowerCase().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String s : p) {
                if (s.isEmpty()) continue;
                if (sb.length() > 0) sb.append(' ');
                sb.append(s.substring(0,1).toUpperCase()).append(s.substring(1));
            }
            nome = sb.toString();
        }
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf != null ? uf.toUpperCase() : null; }

    // Utilitários
    public String getNomeCompleto() {
        return nome + " (" + uf + ")";
    }

    public String getUfMaiuscula() {
        return uf != null ? uf.toUpperCase() : "";
    }

    public boolean isValid() {
        return nome != null && !nome.trim().isEmpty() &&
               uf != null && uf.matches("^[A-Z]{2}$");
    }

    public boolean isUfValida() {
        return uf != null && uf.matches("^[A-Z]{2}$");
    }

    public void normalizarUf() {
        if (uf != null) this.uf = uf.toUpperCase().trim();
    }

    public void normalizarNome() {
        if (nome != null && !nome.trim().isEmpty()) {
            String[] palavras = nome.trim().toLowerCase().split("\\s+");
            StringBuilder nomeNormalizado = new StringBuilder();
            for (String palavra : palavras) {
                if (palavra.length() > 0) {
                    if (nomeNormalizado.length() > 0) nomeNormalizado.append(" ");
                    nomeNormalizado.append(palavra.substring(0,1).toUpperCase())
                                   .append(palavra.substring(1));
                }
            }
            this.nome = nomeNormalizado.toString();
        }
    }

    // equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Estado)) return false;
        Estado estado = (Estado) o;
        return Objects.equals(id, estado.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return uf + " - " + nome; }
}
