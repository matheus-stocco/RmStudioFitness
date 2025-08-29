// src/main/java/com/rmstudio/rmstudiofitness/repositorios/ItemPlanoAulaRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.ItemPlanoAula;
import com.rmstudio.rmstudiofitness.entidades.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPlanoAulaRepository extends JpaRepository<ItemPlanoAula, Long> {

    /**
     * Busca itens por plano de aula
     */
    List<ItemPlanoAula> findByPlanoAulaIdOrderByOrdemAsc(Long planoAulaId);

    /**
     * Busca itens por plano de aula e dia
     */
    List<ItemPlanoAula> findByPlanoAulaIdAndDiaOrderByOrdemAsc(Long planoAulaId, DiaSemana dia);

    /**
     * Busca itens por exercício
     */
    List<ItemPlanoAula> findByExercicioId(Long exercicioId);

    /**
     * Busca itens por dia da semana
     */
    List<ItemPlanoAula> findByDia(DiaSemana dia);

    /**
     * Busca itens com JOIN FETCH
     */
    @Query("SELECT i FROM ItemPlanoAula i " +
           "JOIN FETCH i.exercicio " +
           "JOIN FETCH i.planoAula " +
           "WHERE i.planoAula.id = :planoId")
    List<ItemPlanoAula> findByPlanoAulaIdWithExercicio(@Param("planoId") Long planoId);

    /**
     * Conta itens por plano
     */
    long countByPlanoAulaId(Long planoAulaId);

    /**
     * Conta itens por exercício
     */
    long countByExercicioId(Long exercicioId);

    /**
     * Busca itens por faixa de séries
     */
    List<ItemPlanoAula> findBySeriesBetween(Integer seriesMin, Integer seriesMax);

    /**
     * Busca itens de alta intensidade
     */
    @Query("SELECT i FROM ItemPlanoAula i " +
           "WHERE i.series >= :minSeries " +
           "   OR i.repeticoes >= :minRepeticoes " +
           "   OR i.cargaKg >= :minCarga")
    List<ItemPlanoAula> findAltaIntensidade(@Param("minSeries") Integer minSeries,
                                            @Param("minRepeticoes") Integer minRepeticoes,
                                            @Param("minCarga") Double minCarga);
}
