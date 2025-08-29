// src/main/java/com/rmstudio/rmstudiofitness/repositorios/ExercicioRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Exercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExercicioRepository extends JpaRepository<Exercicio, Long> {

    /**
     * Busca todos os exercícios ordenados por nome
     */
    List<Exercicio> findAllByOrderByNome();

    /**
     * Busca exercícios por grupo muscular
     */
    List<Exercicio> findByGrupoMuscularOrderByNome(String grupoMuscular);

    /**
     * Busca exercícios por grupo muscular (ignoring case)
     */
    List<Exercicio> findByGrupoMuscularIgnoreCaseOrderByNome(String grupoMuscular);

    /**
     * Busca exercício por nome
     */
    Optional<Exercicio> findByNome(String nome);

    /**
     * Busca exercício por nome (ignoring case)
     */
    Optional<Exercicio> findByNomeIgnoreCase(String nome);

    /**
     * Busca exercícios por nome contendo (ignoring case)
     */
    List<Exercicio> findByNomeContainingIgnoreCase(String nome);

    /**
     * Busca exercícios por faixa de séries
     */
    List<Exercicio> findBySeriesBetweenOrderByNome(Integer seriesMin, Integer seriesMax);

    /**
     * Busca exercícios por faixa de repetições
     */
    List<Exercicio> findByRepeticoesBetweenOrderByNome(Integer repMin, Integer repMax);

    /**
     * Busca todos os grupos musculares únicos
     */
    @Query("SELECT DISTINCT e.grupoMuscular FROM Exercicio e ORDER BY e.grupoMuscular")
    List<String> findDistinctGruposMusculares();

    /**
     * Busca exercícios de alta intensidade
     */
    @Query("SELECT e FROM Exercicio e " +
           "WHERE e.series >= :minSeries OR e.repeticoes >= :minRepeticoes " +
           "ORDER BY e.nome")
    List<Exercicio> findAltaIntensidade(@Param("minSeries") Integer minSeries,
                                        @Param("minRepeticoes") Integer minRepeticoes);
}
