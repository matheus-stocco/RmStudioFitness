package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long> {

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

}
