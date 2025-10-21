package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long> {

    @Query(value = "SELECT m FROM Mensalidade m WHERE m.pessoa.id = :pessoaId AND FUNCTION('DATE_TRUNC', 'MONTH', m.dataVencimento) <= FUNCTION('DATE_TRUNC', 'MONTH', CURRENT_DATE) " +
                   "ORDER BY CASE WHEN m.status = 'PENDENTE' THEN 1 ELSE 2 END, m.dataVencimento DESC, m.id DESC",
           countQuery = "SELECT COUNT(m) FROM Mensalidade m WHERE m.pessoa.id = :pessoaId AND FUNCTION('DATE_TRUNC', 'MONTH', m.dataVencimento) <= FUNCTION('DATE_TRUNC', 'MONTH', CURRENT_DATE)")
    Page<Mensalidade> findVisiveisByPessoaIdWithCustomSort(
        @Param("pessoaId") Long pessoaId,
        Pageable pageable
    );

    @Query("SELECT m FROM Mensalidade m " +
           "LEFT JOIN FETCH m.pessoa p " +
           "LEFT JOIN FETCH m.tipoPlano tp " +
           "WHERE m.id IN :ids ORDER BY m.dataVencimento DESC, m.id DESC")
    List<Mensalidade> findAllWithDetailsByIds(@Param("ids") List<Long> ids);

    /**
     * Busca mensalidades de uma pessoa com JOIN FETCH para evitar lazy loading
     */
    @Query("SELECT m FROM Mensalidade m JOIN FETCH m.pessoa p JOIN FETCH m.tipoPlano tp WHERE p.id = :pessoaId ORDER BY m.dataVencimento DESC")
    List<Mensalidade> findByPessoaIdWithDetails(@Param("pessoaId") Long pessoaId);

    @Query("SELECT m FROM Mensalidade m JOIN FETCH m.pessoa p WHERE m.id = :id")
    Optional<Mensalidade> findByIdWithPessoa(@Param("id") Long id);

    @Query("SELECT m FROM Mensalidade m JOIN FETCH m.tipoPlano tp WHERE m.id = :id")
    Optional<Mensalidade> findByIdWithTipoPlano(@Param("id") Long id);

    @Query("SELECT m FROM Mensalidade m JOIN FETCH m.pessoa p JOIN FETCH m.tipoPlano tp WHERE m.id = :id")
    Optional<Mensalidade> findByIdWithPessoaAndTipoPlano(@Param("id") Long id);

    /**
     * Busca todas as mensalidades de uma pessoa, já carregando o TipoPlano associado para evitar N+1 queries.
     */
    @Query("SELECT m FROM Mensalidade m JOIN FETCH m.tipoPlano tp WHERE m.pessoa.id = :pessoaId ORDER BY m.dataVencimento DESC")
    List<Mensalidade> findByPessoaIdWithTipoPlano(@Param("pessoaId") Long pessoaId);

    /**
     * Busca todas as mensalidades de uma pessoa específica, ordenadas pela data de vencimento.
     * @param pessoaId O ID da pessoa.
     * @return Uma lista de mensalidades.
     */
    List<Mensalidade> findByPessoaIdOrderByDataVencimentoDesc(Long pessoaId);

    /**
     * Busca uma mensalidade pelo ID da transação do gateway de pagamento.
     * @param transactionId O ID da transação.
     * @return Um Optional contendo a mensalidade, se encontrada.
     */
    Optional<Mensalidade> findByTransactionId(String transactionId);

    /**
     * Busca todas as mensalidades, já carregando os dados da Pessoa e do TipoPlano.
     * @return Uma lista de mensalidades com as associações carregadas.
     */
    @Query("SELECT m FROM Mensalidade m JOIN FETCH m.pessoa p JOIN FETCH m.tipoPlano tp ORDER BY m.dataVencimento DESC")
    List<Mensalidade> findAllWithDetails();

    /**
     * Busca todas as mensalidades de um determinado ano.
     */
    @Query("SELECT m FROM Mensalidade m JOIN FETCH m.pessoa p JOIN FETCH m.tipoPlano tp WHERE YEAR(m.dataVencimento) = :ano")
    List<Mensalidade> findByYearWithDetails(@Param("ano") int ano);

     /**
     * Busca mensalidades para o relatório com filtros e paginação.
     */
    @Query(value = "SELECT m.* FROM mensalidade m " +
           "JOIN pessoa p ON m.pessoa_id = p.id " +
           "WHERE ( :ano IS NULL OR EXTRACT(YEAR FROM m.data_vencimento) = :ano ) " +
           "AND ( :mes IS NULL OR EXTRACT(MONTH FROM m.data_vencimento) = :mes ) " +
           "AND ( :alunoNome IS NULL OR LOWER(CAST(p.nome AS TEXT)) LIKE LOWER(CONCAT('%', :alunoNome, '%'))) " +
           "AND ( :status IS NULL OR :status = 'TODAS' OR " +
           "      ( :status = 'ATRASADA' AND m.status = 'PENDENTE' AND m.data_vencimento < CURRENT_DATE ) OR " +
           "      ( :status <> 'ATRASADA' AND m.status = :status) ) " +
           "ORDER BY m.data_vencimento DESC",
           countQuery = "SELECT count(*) FROM mensalidade m " +
           "JOIN pessoa p ON m.pessoa_id = p.id " +
           "WHERE ( :ano IS NULL OR EXTRACT(YEAR FROM m.data_vencimento) = :ano ) " +
           "AND ( :mes IS NULL OR EXTRACT(MONTH FROM m.data_vencimento) = :mes ) " +
           "AND ( :alunoNome IS NULL OR LOWER(CAST(p.nome AS TEXT)) LIKE LOWER(CONCAT('%', :alunoNome, '%'))) " +
           "AND ( :status IS NULL OR :status = 'TODAS' OR " +
           "      ( :status = 'ATRASADA' AND m.status = 'PENDENTE' AND m.data_vencimento < CURRENT_DATE ) OR " +
           "      ( :status <> 'ATRASADA' AND m.status = :status) )",
           nativeQuery = true)
    Page<Mensalidade> findForRelatorio(
        @Param("ano") Integer ano,
        @Param("mes") Integer mes,
        @Param("status") String status,
        @Param("alunoNome") String alunoNome,
        Pageable pageable
    );
}
