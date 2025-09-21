// src/main/java/com/rmstudio/rmstudiofitness/repositorios/PessoaRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    /** Busca todas as pessoas ordenadas por nome */
    List<Pessoa> findAllByOrderByNome();

    /** Busca pessoa por nome (ignoring case) */
    List<Pessoa> findByNomeContainingIgnoreCase(String nome);

    /** Busca pessoa por usuário */
    Optional<Pessoa> findByUsuario(String usuario);

    /** Busca pessoa por email */
    Optional<Pessoa> findByEmail(String email);

    /** Busca pessoa por CPF */
    Optional<Pessoa> findByCpf(String cpf);

    /** Verifica se existe pessoa com usuário */
    boolean existsByUsuario(String usuario);

    /** Verifica se existe pessoa com email */
    boolean existsByEmail(String email);

    /** Verifica se existe pessoa com CPF */
    boolean existsByCpf(String cpf);

    /** Busca pessoas ativas */
    @Query("SELECT p FROM Pessoa p WHERE p.ativo = true ORDER BY p.nome")
    List<Pessoa> findPessoasAtivas();

    /** Busca pessoas por cidade */
    List<Pessoa> findByCidadeIdOrderByNome(Long cidadeId);

    /** Busca pessoas por estado (via cidade) */
    @Query("SELECT p FROM Pessoa p JOIN p.cidade c WHERE c.estado.id = :estadoId ORDER BY p.nome")
    List<Pessoa> findByEstadoId(@Param("estadoId") Long estadoId);

    /** Busca pessoas cadastradas em um período */
    List<Pessoa> findByDataCadastroBetweenOrderByDataCadastroDesc(LocalDateTime inicio, LocalDateTime fim);

    /** Busca pessoas por gênero */
    List<Pessoa> findByGeneroOrderByNome(String genero);

    /** Busca pessoas com JOIN FETCH da cidade e estado */
    @Query("SELECT p FROM Pessoa p JOIN FETCH p.cidade c JOIN FETCH c.estado ORDER BY p.nome")
    List<Pessoa> findAllWithCidadeAndEstado();

    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.cidade c LEFT JOIN FETCH c.estado WHERE p.id = :id")
    Optional<Pessoa> findByIdWithCidadeAndEstado(@Param("id") Long id);

    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.avaliacoes WHERE p.id = :id")
    Optional<Pessoa> findByIdWithAvaliacoes(@Param("id") Long id);

    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.avaliacoes WHERE p.usuario = :usuario")
    Optional<Pessoa> findByUsuarioWithAvaliacoes(@Param("usuario") String usuario);

    @Query("SELECT DISTINCT p FROM Pessoa p LEFT JOIN FETCH p.planosDeAula pa LEFT JOIN FETCH pa.itens i LEFT JOIN FETCH i.exercicio WHERE p.usuario = :usuario")
    Optional<Pessoa> findByUsuarioWithPlanosDeAula(@Param("usuario") String usuario);
}
