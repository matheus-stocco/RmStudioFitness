package com.rmstudio.rmstudiofitness.repositorios;

import com.rmstudio.rmstudiofitness.entidades.TokenRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Long> {

    /**
     * Busca um token específico que ainda seja válido (não usado e não expirado)
     */
    @Query("SELECT t FROM TokenRecuperacao t WHERE t.token = :token AND t.usado = false AND t.dataExpiracao > :agora")
    Optional<TokenRecuperacao> findByTokenAndValidoAndNotExpired(@Param("token") String token, @Param("agora") LocalDateTime agora);

    /**
     * Busca tokens válidos por e-mail
     */
    @Query("SELECT t FROM TokenRecuperacao t WHERE t.email = :email AND t.usado = false AND t.dataExpiracao > :agora")
    Optional<TokenRecuperacao> findByEmailAndValidoAndNotExpired(@Param("email") String email, @Param("agora") LocalDateTime agora);

    /**
     * Remove todos os tokens expirados (limpeza automática)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenRecuperacao t WHERE t.dataExpiracao < :agora")
    void deleteExpiredTokens(@Param("agora") LocalDateTime agora);

    /**
     * Invalida todos os tokens de um e-mail específico
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenRecuperacao t SET t.usado = true WHERE t.email = :email")
    void invalidateTokensByEmail(@Param("email") String email);
}
