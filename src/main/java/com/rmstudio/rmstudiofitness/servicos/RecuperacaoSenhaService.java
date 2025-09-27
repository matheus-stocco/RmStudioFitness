package com.rmstudio.rmstudiofitness.servicos;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.entidades.TokenRecuperacao;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import com.rmstudio.rmstudiofitness.repositorios.TokenRecuperacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável pela lógica de recuperação de senha.
 */
@Service
public class RecuperacaoSenhaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private TokenRecuperacaoRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Inicia o processo de recuperação de senha para um e-mail específico
     * 
     * @param email E-mail do usuário
     * @return true se o e-mail foi enviado (mesmo que o usuário não exista, por segurança)
     */
    @Transactional
    public boolean iniciarRecuperacao(String email) {
        Optional<Pessoa> pessoaOpt = pessoaRepository.findByEmail(email.trim().toLowerCase());
        
        if (pessoaOpt.isPresent()) {
            Pessoa pessoa = pessoaOpt.get();
            
            // Invalida tokens anteriores para este e-mail
            tokenRepository.invalidateTokensByEmail(email);
            
            // Cria novo token
            String token = UUID.randomUUID().toString();
            TokenRecuperacao tokenRecuperacao = new TokenRecuperacao(token, email);
            
            tokenRepository.save(tokenRecuperacao);
            
            // Envia e-mail
            emailService.enviarEmailRecuperacaoSenha(email, pessoa.getNome(), token);
        }
        
        // Sempre retorna true por questões de segurança (não revela se o e-mail existe)
        return true;
    }

    /**
     * Valida se um token de recuperação é válido
     * 
     * @param token Token a ser validado
     * @return true se o token é válido
     */
    public boolean validarToken(String token) {
        return tokenRepository.findByTokenAndValidoAndNotExpired(token, LocalDateTime.now()).isPresent();
    }

    /**
     * Redefine a senha do usuário usando um token válido
     * 
     * @param token Token de recuperação
     * @param novaSenha Nova senha (será criptografada)
     * @return true se a senha foi alterada com sucesso
     */
    @Transactional
    public boolean redefinirSenha(String token, String novaSenha) {
        Optional<TokenRecuperacao> tokenOpt = tokenRepository.findByTokenAndValidoAndNotExpired(token, LocalDateTime.now());
        
        if (tokenOpt.isEmpty()) {
            return false;
        }

        TokenRecuperacao tokenRecuperacao = tokenOpt.get();
        Optional<Pessoa> pessoaOpt = pessoaRepository.findByEmail(tokenRecuperacao.getEmail());
        
        if (pessoaOpt.isEmpty()) {
            return false;
        }

        Pessoa pessoa = pessoaOpt.get();
        
        // Atualiza a senha
        pessoa.setSenha(passwordEncoder.encode(novaSenha));
        pessoaRepository.save(pessoa);
        
        // Marca o token como usado
        tokenRecuperacao.setUsado(true);
        tokenRepository.save(tokenRecuperacao);
        
        // Envia e-mail de confirmação
        emailService.enviarEmailConfirmacaoTrocaSenha(pessoa.getEmail(), pessoa.getNome());
        
        return true;
    }

    /**
     * Busca informações básicas do usuário pelo token (para exibição na tela de redefinição)
     * 
     * @param token Token de recuperação
     * @return E-mail do usuário ou null se token inválido
     */
    public String obterEmailPorToken(String token) {
        Optional<TokenRecuperacao> tokenOpt = tokenRepository.findByTokenAndValidoAndNotExpired(token, LocalDateTime.now());
        
        return tokenOpt.map(TokenRecuperacao::getEmail).orElse(null);
    }

    /**
     * Limpeza automática de tokens expirados - executa de hora em hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora em milissegundos
    @Transactional
    public void limparTokensExpirados() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
