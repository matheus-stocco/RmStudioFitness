package com.rmstudio.rmstudiofitness.dtos;

import com.rmstudio.rmstudiofitness.entidades.PlanoDeAula;

import java.time.LocalDateTime;

public class PlanoDeAulaDTO {

    private Long id;
    private String nome;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private AlunoDTO aluno;

    // Construtor que converte a entidade para DTO
    public PlanoDeAulaDTO(PlanoDeAula planoDeAula) {
        this.id = planoDeAula.getId();
        this.nome = planoDeAula.getNome();
        this.descricao = planoDeAula.getDescricao();
        this.dataInicio = planoDeAula.getDataInicio();
        this.dataFim = planoDeAula.getDataFim();
        if (planoDeAula.getAluno() != null) {
            this.aluno = new AlunoDTO(planoDeAula.getAluno().getId(), planoDeAula.getAluno().getNome());
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public AlunoDTO getAluno() {
        return aluno;
    }

    // DTO aninhado para o aluno, para não expor a entidade Pessoa inteira
    public static class AlunoDTO {
        private Long id;
        private String nome;

        public AlunoDTO(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        // Getters
        public Long getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }
    }
}
