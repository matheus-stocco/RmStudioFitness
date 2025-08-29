// src/main/java/com/rmstudio/rmstudiofitness/repositorios/CidadeRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Long> {

    /**
     * Busca todas as cidades ordenadas por nome
     */
    List<Cidade> findAllByOrderByNome();

    /**
     * Busca cidades por estado
     */
    List<Cidade> findByEstadoIdOrderByNome(Long estadoId);

    /**
     * Busca cidade por nome (ignoring case)
     */
    List<Cidade> findByNomeContainingIgnoreCase(String nome);

    /**
     * Busca cidade por nome exato e estado
     */
    Optional<Cidade> findByNomeAndEstadoId(String nome, Long estadoId);

    /**
     * Busca cidades com JOIN FETCH do estado
     */
    @Query("SELECT c FROM Cidade c JOIN FETCH c.estado ORDER BY c.nome")
    List<Cidade> findAllWithEstado();

    /**
     * Busca cidades de um estado específico com JOIN FETCH
     */
    @Query("SELECT c FROM Cidade c JOIN FETCH c.estado e WHERE e.uf = :uf ORDER BY c.nome")
    List<Cidade> findByEstadoUf(@Param("uf") String uf);
}
