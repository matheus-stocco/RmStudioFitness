package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import com.rmstudio.rmstudiofitness.servicos.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.rmstudio.rmstudiofitness.repositorios.MensalidadeRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.rmstudio.rmstudiofitness.paghiper.dto.PagHiperNotificationRequest;
import org.springframework.security.core.Authentication;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControlePagamento {

    private final PagamentoService pagamentoService;
    private final MensalidadeRepository mensalidadeRepository;
    private final PessoaRepository pessoaRepository;

    public ControlePagamento(PagamentoService pagamentoService, MensalidadeRepository mensalidadeRepository, PessoaRepository pessoaRepository) {
        this.pagamentoService = pagamentoService;
        this.mensalidadeRepository = mensalidadeRepository;
        this.pessoaRepository = pessoaRepository;
    }

    /**
     * Endpoint para o administrador atribuir um plano a um aluno.
     * Isso define o plano ativo do aluno e gera a primeira cobrança PENDENTE.
     */
    @PostMapping("/api/pagamentos/atribuir-plano")
    @ResponseBody
    public ResponseEntity<Mensalidade> atribuirPlanoAdmin(@RequestBody AtribuirPlanoRequest request) {
        Mensalidade novaMensalidade = pagamentoService.atribuirPlano(request.pessoaId(), request.tipoPlanoId());
        return ResponseEntity.ok(novaMensalidade);
    }
    
    /**
     * Mapeia a URL para a página de pagamento de uma mensalidade específica.
     */
    @GetMapping("/api/pagamentos/pagar/{id}")
    public String pagarMensalidade(@PathVariable Long id, Model model, Authentication authentication) {
        String username = authentication.getName();
        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Mensalidade mensalidade = pagamentoService.findMensalidadeParaPagamento(id);

        // Validação: Garante que a mensalidade pertence ao usuário logado
        if (!mensalidade.getPessoa().getId().equals(pessoa.getId())) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar esta cobrança.");
        }

        model.addAttribute("mensalidade", mensalidade);
        model.addAttribute("pageTitle", "Pagamento PIX");
        return "pagar-mensalidade";
    }

    /**
     * Endpoint para receber notificações de webhook da PagHiper.
     */
    @PostMapping("/api/pagamentos/notificacao")
    public ResponseEntity<Void> receberNotificacao(@RequestBody PagHiperNotificationRequest notification) {
        pagamentoService.processarNotificacao(notification);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para um usuário LOGADO se inscrever em um plano.
     */
    @PostMapping("/planos/inscrever/{planoId}")
    public String inscreverPlano(@PathVariable Long planoId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Pessoa pessoa = pessoaRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        pagamentoService.atribuirPlano(pessoa.getId(), planoId);

        // Redireciona o usuário para a tela de mensalidades onde a nova cobrança aparecerá
        return "redirect:/minhas-mensalidades";
    }

    /**
     * Endpoint para gerar o PIX para uma mensalidade pendente.
     */
    @PostMapping("/api/pagamentos/gerar-pix/{mensalidadeId}")
    public String gerarPix(@PathVariable Long mensalidadeId, Authentication authentication) {
        // Validação de segurança: garantir que o usuário logado é o dono da mensalidade (opcional mas recomendado)
        String username = authentication.getName();
        Pessoa pessoa = pessoaRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        
        Mensalidade mensalidade = mensalidadeRepository.findByIdWithPessoa(mensalidadeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensalidade não encontrada."));

        if (!mensalidade.getPessoa().getId().equals(pessoa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar esta cobrança.");
        }
        
        pagamentoService.gerarPixParaMensalidade(mensalidadeId);
        
        return "redirect:/api/pagamentos/pagar/" + mensalidadeId;
    }

    @PostMapping("/api/pagamentos/cancelar-plano")
    public String cancelarPlano(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Pessoa pessoa = pessoaRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        try {
            pagamentoService.cancelarPlano(pessoa.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Seu plano foi cancelado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao cancelar o plano: " + e.getMessage());
        }

        return "redirect:/minhas-mensalidades";
    }

    // DTO para receber os dados da requisição
    public record AtribuirPlanoRequest(Long pessoaId, Long tipoPlanoId) {}
}
