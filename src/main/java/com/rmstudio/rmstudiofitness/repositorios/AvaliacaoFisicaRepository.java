// src/main/java/com/rmstudio/rmstudiofitness/repositorios/AvaliacaoFisicaRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.AvaliacaoFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoFisicaRepository extends JpaRepository<AvaliacaoFisica, Long> {

    /**
     * Busca todas as avaliações ordenadas por data decrescente
     */
    List<AvaliacaoFisica> findAllByOrderByDataAvaliacaoDesc();

    /**
     * Busca avaliações de uma pessoa específica ordenadas por data
     */
    List<AvaliacaoFisica> findByPessoaIdOrderByDataAvaliacaoDesc(Long pessoaId);

    /**
     * Busca avaliações por período
     */
    List<AvaliacaoFisica> findByDataAvaliacaoBetweenOrderByDataAvaliacaoDesc(
            LocalDate dataInicio, LocalDate dataFim);

    /**
     * Busca a avaliação mais recente de uma pessoa
     */
    @Query("SELECT a FROM AvaliacaoFisica a WHERE a.pessoa.id = :pessoaId ORDER BY a.dataAvaliacao DESC")
    Optional<AvaliacaoFisica> findMostRecentByPessoaId(@Param("pessoaId") Long pessoaId);

    /**
     * Conta o número de avaliações de uma pessoa
     */
    long countByPessoaId(Long pessoaId);

    /**
     * Busca avaliações por faixa de peso
     */
    List<AvaliacaoFisica> findByPesoBetweenOrderByDataAvaliacaoDesc(
            java.math.BigDecimal pesoMin, java.math.BigDecimal pesoMax);
}
