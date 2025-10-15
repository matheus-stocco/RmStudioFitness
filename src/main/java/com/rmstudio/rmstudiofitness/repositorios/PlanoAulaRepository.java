// src/main/java/com/rmstudio/rmstudiofitness/repositorios/PlanoAulaRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.PlanoAula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;

@Repository
public interface PlanoAulaRepository extends JpaRepository<PlanoAula, Long> {

    /** Busca planos por ID de aluno com paginação */
    Page<PlanoAula> findByAluno_IdOrderByDataInicioDesc(Long alunoId, Pageable pageable);

    /** Busca planos por ID de aluno com paginação */
    Page<PlanoAula> findByAlunoOrderByDataInicioDesc(Pessoa aluno, Pageable pageable);

    @Query("SELECT DISTINCT p FROM PlanoAula p LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.exercicio WHERE p.id IN :ids ORDER BY p.dataInicio DESC")
    List<PlanoAula> findAllWithDetailsByIds(@Param("ids") List<Long> ids);

    /** Busca todos os planos ordenados por nome com paginação */
    Page<PlanoAula> findAllByOrderByNome(Pageable pageable);

    /** Busca todos os planos ordenados por nome */
    List<PlanoAula> findAllByOrderByNome();

    /** Busca planos ativos */
    List<PlanoAula> findByAtivoTrueOrderByNome();

    /** Busca plano por nome */
    Optional<PlanoAula> findByNome(String nome);

    /** Busca planos por nome contendo */
    List<PlanoAula> findByNomeContainingIgnoreCase(String nome);

    /** Busca planos por nível de dificuldade */
    List<PlanoAula> findByNivelDificuldadeOrderByNome(String nivelDificuldade);

    /** Busca planos criados em um período */
    List<PlanoAula> findByDataCriacaoBetweenOrderByDataCriacaoDesc(LocalDateTime inicio, LocalDateTime fim);

    /** Busca planos com JOIN FETCH dos itens */
    @Query("SELECT DISTINCT p FROM PlanoAula p LEFT JOIN FETCH p.itens ORDER BY p.nome")
    List<PlanoAula> findAllWithItens();

    /** Busca plano por ID com JOIN FETCH dos itens e seus exercícios */
    @Query("SELECT DISTINCT p FROM PlanoAula p LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.exercicio WHERE p.id = :id")
    Optional<PlanoAula> findByIdWithItens(@Param("id") Long id);

    /** Conta planos ativos */
    long countByAtivoTrue();

    /** Busca planos por objetivo */
    List<PlanoAula> findByObjetivoContainingIgnoreCase(String objetivo);

    /** Busca planos mais recentes */
    List<PlanoAula> findTop10ByAtivoTrueOrderByDataCriacaoDesc();
}
