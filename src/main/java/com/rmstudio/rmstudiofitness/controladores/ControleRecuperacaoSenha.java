package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.servicos.RecuperacaoSenhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador responsável pelas funcionalidades de recuperação de senha.
 */
@Controller
@RequestMapping("/")
public class ControleRecuperacaoSenha {

    @Autowired
    private RecuperacaoSenhaService recuperacaoSenhaService;

    /**
     * Exibe a página para solicitar recuperação de senha
     */
    @GetMapping("/esqueceu-senha")
    public String exibirPaginaEsqueceuSenha() {
        return "esqueceu-senha";
    }

    /**
     * Processa a solicitação de recuperação de senha
     */
    @PostMapping("/esqueceu-senha")
    public String processarSolicitacaoRecuperacao(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        try {
            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("erro", "Por favor, informe seu e-mail.");
                return "redirect:/esqueceu-senha";
            }

            recuperacaoSenhaService.iniciarRecuperacao(email.trim());
            
            redirectAttributes.addFlashAttribute("sucesso", 
                "Se o e-mail informado estiver cadastrado em nosso sistema, " +
                "você receberá as instruções para recuperar sua senha.");
            
            return "redirect:/esqueceu-senha";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", 
                "Ocorreu um erro ao processar sua solicitação. Tente novamente.");
            return "redirect:/esqueceu-senha";
        }
    }

    /**
     * Exibe a página para redefinir a senha com o token
     */
    @GetMapping("/recuperar-senha")
    public String exibirPaginaRecuperarSenha(
            @RequestParam("token") String token,
            Model model) {

        if (!recuperacaoSenhaService.validarToken(token)) {
            model.addAttribute("erro", 
                "Link de recuperação inválido ou expirado. " +
                "Solicite uma nova recuperação de senha.");
            return "erro-recuperacao";
        }

        String email = recuperacaoSenhaService.obterEmailPorToken(token);
        model.addAttribute("token", token);
        model.addAttribute("email", email);
        
        return "recuperar-senha";
    }

    /**
     * Processa a redefinição da senha
     */
    @PostMapping("/recuperar-senha")
    public String processarRedefinicaoSenha(
            @RequestParam("token") String token,
            @RequestParam("novaSenha") String novaSenha,
            @RequestParam("confirmarSenha") String confirmarSenha,
            RedirectAttributes redirectAttributes) {

        try {
            // Validações
            if (novaSenha == null || novaSenha.trim().length() < 6) {
                redirectAttributes.addFlashAttribute("erro", "A senha deve ter pelo menos 6 caracteres.");
                return "redirect:/recuperar-senha?token=" + token;
            }

            if (!novaSenha.equals(confirmarSenha)) {
                redirectAttributes.addFlashAttribute("erro", "As senhas não coincidem.");
                return "redirect:/recuperar-senha?token=" + token;
            }

            // Tenta redefinir a senha
            if (recuperacaoSenhaService.redefinirSenha(token, novaSenha)) {
                redirectAttributes.addFlashAttribute("sucesso", 
                    "Sua senha foi alterada com sucesso! Você já pode fazer login com a nova senha.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("erro", 
                    "Link de recuperação inválido ou expirado. " +
                    "Solicite uma nova recuperação de senha.");
                return "redirect:/esqueceu-senha";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", 
                "Ocorreu um erro ao alterar sua senha. Tente novamente.");
            return "redirect:/recuperar-senha?token=" + token;
        }
    }
}
