// src/main/java/com/rmstudio/rmstudiofitness/repositorios/EstadoRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {

    /**
     * Busca todos os estados ordenados por nome
     */
    List<Estado> findAllByOrderByNome();

    /**
     * Busca estado por UF
     */
    Optional<Estado> findByUf(String uf);

    /**
     * Busca estado por UF (ignoring case)
     */
    Optional<Estado> findByUfIgnoreCase(String uf);

    /**
     * Busca estado por nome (ignoring case)
     */
    Optional<Estado> findByNomeIgnoreCase(String nome);

    /**
     * Busca estados por nome contendo (ignoring case)
     */
    List<Estado> findByNomeContainingIgnoreCase(String nome);

    /**
     * Verifica se existe estado com UF
     */
    boolean existsByUf(String uf);

    /**
     * Verifica se existe estado com UF (ignoring case)
     */
    boolean existsByUfIgnoreCase(String uf);
}
