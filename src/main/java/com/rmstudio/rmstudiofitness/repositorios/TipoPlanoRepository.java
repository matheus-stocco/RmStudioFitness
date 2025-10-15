// src/main/java/com/rmstudio/rmstudiofitness/repositorios/TipoPlanoRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.TipoPlano;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TipoPlanoRepository extends JpaRepository<TipoPlano, Long> {

    /** Busca todos os tipos de plano ordenados por nome com paginação */
    Page<TipoPlano> findAllByOrderByNome(Pageable pageable);

    /** Busca todos os tipos de plano ordenados por nome */
    List<TipoPlano> findAllByOrderByNome();

    /** Busca tipos de plano ativos */
    List<TipoPlano> findByAtivoTrueOrderByValor();

    /** Busca tipo de plano por nome */
    Optional<TipoPlano> findByNome(String nome);

    /** Busca tipos por nome contendo */
    List<TipoPlano> findByNomeContainingIgnoreCase(String nome);

    /** Busca tipos por categoria */
    List<TipoPlano> findByCategoriaOrderByValor(String categoria);

    /** Busca tipos por faixa de valor */
    List<TipoPlano> findByValorBetweenOrderByValor(BigDecimal valorMin, BigDecimal valorMax);

    /** Busca tipos por duração */
    List<TipoPlano> findByDuracaoMesesOrderByValor(Integer duracaoMeses);

    /** Busca tipos com acesso a planos de aula */
    List<TipoPlano> findByAcessoPlanosAulaTrueOrderByValor();

    /** Busca tipos criados em um período */
    List<TipoPlano> findByDataCriacaoBetweenOrderByDataCriacaoDesc(LocalDateTime inicio, LocalDateTime fim);

    /** Busca tipos mais baratos */
    @Query("SELECT t FROM TipoPlano t WHERE t.ativo = true ORDER BY t.valor ASC")
    List<TipoPlano> findCheapestPlans();

    /** Busca tipos por valor mensal calculado (valor / duracaoMeses, tratando null como 1) */
    @Query("""
           SELECT t
             FROM TipoPlano t
            WHERE t.ativo = true
              AND (t.valor / COALESCE(t.duracaoMeses, 1)) BETWEEN :valorMin AND :valorMax
         ORDER BY (t.valor / COALESCE(t.duracaoMeses, 1)) ASC
           """)
    List<TipoPlano> findByValorMensalBetween(@Param("valorMin") BigDecimal valorMin,
                                             @Param("valorMax") BigDecimal valorMax);

    /** Conta tipos ativos */
    long countByAtivoTrue();

    /** Verifica se existe tipo com nome */
    boolean existsByNome(String nome);
}
