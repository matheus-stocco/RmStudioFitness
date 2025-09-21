package com.rmstudio.rmstudiofitness.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "plano_de_aula")
public class PlanoDeAula implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = true, length = 100)
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa aluno;

    @OneToMany(mappedBy = "planoDeAula", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemPlanoDeAula> itens = new HashSet<>();

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
    
    @Transient
    public Map<String, List<ItemPlanoDeAula>> getItensAgrupadosPorDia() {
        if (itens == null || itens.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Usa um mapa que preserva a ordem de inserção para manter os dias ordenados
        final Map<String, List<ItemPlanoDeAula>> grouped = new LinkedHashMap<>();

        // Define a ordem correta dos dias da semana
        final List<String> dayOrder = Arrays.asList("SEGUNDA", "TERÇA", "QUARTA", "QUINTA", "SEXTA", "SÁBADO", "DOMINGO");

        // Agrupa os itens pelo dia da semana
        Map<String, List<ItemPlanoDeAula>> itemsByDay = itens.stream()
            .collect(Collectors.groupingBy(ItemPlanoDeAula::getDiaSemana));

        // Ordena os dias agrupados de acordo com a lista `dayOrder` e os insere no mapa final
        itemsByDay.keySet().stream()
            .sorted(Comparator.comparingInt(day -> {
                if (day == null) return dayOrder.size();
                // Normaliza o nome do dia para a comparação (ex: "Segunda-feira" -> "SEGUNDA")
                String normalized = day.toUpperCase().replace("-FEIRA", "");
                int index = dayOrder.indexOf(normalized);
                return index == -1 ? dayOrder.size() : index; // Dias desconhecidos vão para o final
            }))
            .forEach(day -> grouped.put(day, itemsByDay.get(day)));
            
        return grouped;
    }

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Pessoa getAluno() {
        return aluno;
    }

    public void setAluno(Pessoa aluno) {
        this.aluno = aluno;
    }

    public Set<ItemPlanoDeAula> getItens() {
        return itens;
    }

    public void setItens(Set<ItemPlanoDeAula> itens) {
        this.itens.clear();
        if (itens != null) {
            for(ItemPlanoDeAula item : itens) {
                this.addItem(item);
            }
        }
    }

    public void addItem(ItemPlanoDeAula item) {
        this.itens.add(item);
        item.setPlanoDeAula(this);
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
