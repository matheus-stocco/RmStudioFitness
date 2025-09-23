package com.rmstudio.rmstudiofitness.dtos;

import com.rmstudio.rmstudiofitness.entidades.PlanoDeAula;

import java.time.format.DateTimeFormatter;

public class PlanoDeAulaDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String dataInicio;
    private String dataFim;
    private AlunoDTO aluno;

    // Construtor que converte a entidade para DTO
    public PlanoDeAulaDTO(PlanoDeAula planoDeAula) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.id = planoDeAula.getId();
        this.nome = planoDeAula.getNome();
        this.descricao = planoDeAula.getDescricao();
        if (planoDeAula.getDataInicio() != null) {
            this.dataInicio = planoDeAula.getDataInicio().format(formatter);
        }
        if (planoDeAula.getDataFim() != null) {
            this.dataFim = planoDeAula.getDataFim().format(formatter);
        }
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

    public String getDataInicio() {
        return dataInicio;
    }

    public String getDataFim() {
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
