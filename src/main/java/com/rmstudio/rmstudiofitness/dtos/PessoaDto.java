package com.rmstudio.rmstudiofitness.dtos;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import java.util.Set;
import java.util.stream.Collectors;

public class PessoaDto {

    private Long id;
    private String nome;
    private String email;
    private Set<String> perfis;

    public PessoaDto(Pessoa pessoa) {
        this.id = pessoa.getId();
        this.nome = pessoa.getNome();
        this.email = pessoa.getEmail();
        this.perfis = pessoa.getPerfis().stream()
                .map(perfil -> perfil.getNome())
                .collect(Collectors.toSet());
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<String> perfis) {
        this.perfis = perfis;
    }
}
