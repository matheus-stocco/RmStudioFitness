package com.rmstudio.rmstudiofitness.entidades;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity
@Table(name = "pessoa")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pessoa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario", nullable = false, unique = true, length = 30)
    private String usuario;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "senha", nullable = false, length = 100)
    private String senha;

    @Column(name = "email", nullable = false, unique = true, length = 60)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "genero", length = 1) // "M","F","O"
    private String genero;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "cpf", length = 14, unique = true)
    private String cpf;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    // Muitas pessoas para uma cidade
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cidade_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cidade cidade;

    // Construtores
    public Pessoa() {
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
    }

    public Pessoa(String usuario, String nome, String email, Cidade cidade) {
        this();
        this.usuario = usuario;
        this.nome = nome;
        this.email = email;
        this.cidade = cidade;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCadastro == null) this.dataCadastro = LocalDateTime.now();
        if (this.ativo == null) this.ativo = true;
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Cidade getCidade() { return cidade; }
    public void setCidade(Cidade cidade) { this.cidade = cidade; }

    // Auxiliares (mantidos como no seu modelo)
    @Transient
    public Estado getEstado() { return (cidade != null) ? cidade.getEstado() : null; }

    public String getDataCadastroFormatada() {
        if (dataCadastro == null) return "";
        return dataCadastro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getDataNascimentoFormatada() {
        if (dataNascimento == null) return "";
        return dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Transient
    public Integer getIdade() {
        if (dataNascimento == null) return null;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    @Transient
    public String getGeneroDescricao() {
        if (genero == null) return "Não informado";
        switch (genero.toUpperCase()) {
            case "M": return "Masculino";
            case "F": return "Feminino";
            case "O": return "Outro";
            default:  return "Não informado";
        }
    }

    @Transient
    public String getLocalizacaoCompleta() {
        if (cidade == null) return "";
        return cidade.getNomeCompleto();
    }

    @Transient
    public boolean isValid() {
        return usuario != null && !usuario.trim().isEmpty() &&
               nome != null && !nome.trim().isEmpty() &&
               senha != null && !senha.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               cidade != null;
    }

    @Transient
    public boolean isEmailValido() {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    @Transient
    public boolean isCpfValido() {
        if (cpf == null) return true; // opcional
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        return cpfLimpo.length() == 11;
    }

    @Transient
    public String getCpfFormatado() {
        if (cpf == null) return "";
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() == 11) {
            return cpfLimpo.substring(0,3)+"."+cpfLimpo.substring(3,6)+"."+
                   cpfLimpo.substring(6,9)+"-"+cpfLimpo.substring(9);
        }
        return cpf;
    }

    public void normalizar() {
        if (nome != null) nome = nome.trim();
        if (usuario != null) usuario = usuario.trim().toLowerCase();
        if (email != null) email = email.trim().toLowerCase();
        if (genero != null) genero = genero.toUpperCase();
        if (cpf != null) cpf = cpf.replaceAll("[^0-9]", "");
    }

    @Transient
    public boolean isAtiva() { return ativo != null && ativo; }

    @Transient
    public String getInfoBasica() {
        StringBuilder info = new StringBuilder(nome);
        if (getIdade() != null) info.append(" (").append(getIdade()).append(" anos)");
        if (cidade != null) info.append(" - ").append(cidade.getNomeCompleto());
        return info.toString();
    }

    // equals/hashCode por id
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pessoa)) return false;
        Pessoa other = (Pessoa) obj;
        return Objects.equals(id, other.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Pessoa{id=" + id + ", nome='" + nome + "', usuario='" + usuario +
               "', email='" + email + "', ativo=" + ativo + "}";
    }
}
