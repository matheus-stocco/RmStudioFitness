// src/main/java/com/rmstudio/rmstudiofitness/repositorios/CidadeRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Cidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Long> {

    Page<Cidade> findAllByOrderByNome(Pageable pageable);

    Page<Cidade> findByEstadoIdOrderByNome(Long estadoId, Pageable pageable);

    Page<Cidade> findByNomeContainingIgnoreCaseOrderByNome(String q, Pageable pageable);

    Page<Cidade> findByNomeContainingIgnoreCaseAndEstadoIdOrderByNome(String q, Long estadoId, Pageable pageable);
}
