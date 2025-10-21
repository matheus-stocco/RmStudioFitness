// src/main/java/com/rmstudio/rmstudiofitness/repositorios/PessoaRepository.java
package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    /** Busca pessoa por nome (ignoring case) com paginação */
    Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /** Busca pessoa por nome (ignoring case) */
    List<Pessoa> findByNomeContainingIgnoreCase(String nome);

    /** Busca pessoa por nome e perfil (ignoring case) */
    List<Pessoa> findByNomeContainingIgnoreCaseAndPerfis_Nome(String nome, String perfilNome);

    /** Busca todas as pessoas ordenadas por nome */
    List<Pessoa> findAllByOrderByNome();

    Page<Pessoa> findByPlanoAtivoIsNotNull(Pageable pageable);
    List<Pessoa> findByPlanoAtivoIsNotNullOrderByNome();
    Page<Pessoa> findByNomeContainingIgnoreCaseAndPlanoAtivoIsNotNull(String nome, Pageable pageable);
    List<Pessoa> findByNomeContainingIgnoreCaseAndPlanoAtivoIsNotNull(String nome);

    Page<Pessoa> findByPlanoAtivoIsNull(Pageable pageable);
    List<Pessoa> findByPlanoAtivoIsNullOrderByNome();
    Page<Pessoa> findByNomeContainingIgnoreCaseAndPlanoAtivoIsNull(String nome, Pageable pageable);
    List<Pessoa> findByNomeContainingIgnoreCaseAndPlanoAtivoIsNull(String nome);

    /** Busca pessoa por usuário */
    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.planoAtivo WHERE p.usuario = :usuario")
    Optional<Pessoa> findByUsuario(@Param("usuario") String usuario);

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

    /** Busca pessoas por estado (via cidade) com paginação */
    @Query(value = "SELECT p FROM Pessoa p JOIN p.cidade c WHERE c.estado.id = :estadoId",
           countQuery = "SELECT count(p) FROM Pessoa p JOIN p.cidade c WHERE c.estado.id = :estadoId")
    Page<Pessoa> findByEstadoId(@Param("estadoId") Long estadoId, Pageable pageable);

    /** Busca pessoas por cidade com paginação */
    Page<Pessoa> findByCidadeIdOrderByNome(Long cidadeId, Pageable pageable);

    /** Busca pessoas cadastradas em um período */
    List<Pessoa> findByDataCadastroBetweenOrderByDataCadastroDesc(LocalDateTime inicio, LocalDateTime fim);

    /** Busca pessoas por gênero */
    List<Pessoa> findByGeneroOrderByNome(String genero);

    /** Busca pessoas com JOIN FETCH da cidade e estado */
    @Query("SELECT p FROM Pessoa p JOIN FETCH p.cidade c JOIN FETCH c.estado ORDER BY p.nome")
    List<Pessoa> findAllWithCidadeAndEstado();

    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.cidade c LEFT JOIN FETCH c.estado LEFT JOIN FETCH p.planoAtivo WHERE p.id = :id")
    Optional<Pessoa> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.avaliacoes WHERE p.id = :id")
    Optional<Pessoa> findByIdWithAvaliacoes(@Param("id") Long id);

    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.avaliacoes WHERE p.usuario = :usuario")
    Optional<Pessoa> findByUsuarioWithAvaliacoes(@Param("usuario") String usuario);

    @Query("SELECT DISTINCT p FROM Pessoa p LEFT JOIN FETCH p.planosDeAula pa LEFT JOIN FETCH pa.itens i LEFT JOIN FETCH i.exercicio WHERE p.usuario = :usuario")
    Optional<Pessoa> findByUsuarioWithPlanosDeAula(@Param("usuario") String usuario);

    /** Contagem de membros por status (ativo/ocioso) */
    long countByPlanoAtivoIsNotNull();
    long countByPlanoAtivoIsNull();

    /** Contagem de membros por gênero */
    long countByGenero(String genero);
}
