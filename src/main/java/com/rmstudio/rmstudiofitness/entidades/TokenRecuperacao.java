package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um token de recuperação de senha.
 * Cada token é temporário e válido por um período limitado.
 */
@Entity
@Table(name = "token_recuperacao")
public class TokenRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "email", nullable = false, length = 60)
    private String email;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    // Construtores
    public TokenRecuperacao() {
        this.dataCriacao = LocalDateTime.now();
        this.dataExpiracao = LocalDateTime.now().plusHours(1); // Token válido por 1 hora
        this.usado = false;
    }

    public TokenRecuperacao(String token, String email) {
        this();
        this.token = token;
        this.email = email;
    }

    // Métodos de conveniência
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(this.dataExpiracao);
    }

    public boolean isValido() {
        return !usado && !isExpirado();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    @Override
    public String toString() {
        return "TokenRecuperacao{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", dataCriacao=" + dataCriacao +
                ", dataExpiracao=" + dataExpiracao +
                ", usado=" + usado +
                '}';
    }
}
