package com.rmstudio.rmstudiofitness.servicos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pelo envio de e-mails do sistema.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@rmstudiofitness.com}")
    private String remetenteEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Envia e-mail de recuperação de senha para o usuário
     * 
     * @param destinatario Email do destinatário
     * @param nomeUsuario Nome do usuário para personalizar a mensagem
     * @param token Token de recuperação
     */
    public void enviarEmailRecuperacaoSenha(String destinatario, String nomeUsuario, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetenteEmail);
            message.setTo(destinatario);
            message.setSubject("RMStudio Fitness - Recuperação de Senha");
            
            String linkRecuperacao = baseUrl + "/recuperar-senha?token=" + token;
            
            String corpoMensagem = String.format(
                "Olá %s!\n\n" +
                "Você solicitou a recuperação de sua senha no RMStudio Fitness.\n\n" +
                "Para criar uma nova senha, clique no link abaixo:\n" +
                "%s\n\n" +
                "Este link é válido por 1 hora e só pode ser usado uma vez.\n\n" +
                "Se você não solicitou esta recuperação, pode ignorar este e-mail com segurança.\n\n" +
                "Atenciosamente,\n" +
                "Equipe RMStudio Fitness\n\n" +
                "---\n" +
                "Este é um e-mail automático, não responda a esta mensagem.",
                nomeUsuario, linkRecuperacao
            );
            
            message.setText(corpoMensagem);
            
            mailSender.send(message);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar e-mail de recuperação de senha", e);
        }
    }

    /**
     * Envia e-mail de confirmação após troca de senha
     * 
     * @param destinatario Email do destinatário
     * @param nomeUsuario Nome do usuário
     */
    public void enviarEmailConfirmacaoTrocaSenha(String destinatario, String nomeUsuario) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetenteEmail);
            message.setTo(destinatario);
            message.setSubject("RMStudio Fitness - Senha Alterada com Sucesso");
            
            String corpoMensagem = String.format(
                "Olá %s!\n\n" +
                "Sua senha no RMStudio Fitness foi alterada com sucesso.\n\n" +
                "Se você não fez esta alteração, entre em contato conosco imediatamente.\n\n" +
                "Atenciosamente,\n" +
                "Equipe RMStudio Fitness\n\n" +
                "---\n" +
                "Este é um e-mail automático, não responda a esta mensagem.",
                nomeUsuario
            );
            
            message.setText(corpoMensagem);
            
            mailSender.send(message);
            
        } catch (Exception e) {
            // Log do erro, mas não falha o processo principal
            System.err.println("Erro ao enviar e-mail de confirmação: " + e.getMessage());
        }
    }
}
