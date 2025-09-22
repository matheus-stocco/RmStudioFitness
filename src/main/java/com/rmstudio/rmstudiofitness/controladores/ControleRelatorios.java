package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import com.rmstudio.rmstudiofitness.servicos.PagamentoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.math.BigDecimal;
import java.util.Map;

@Controller
@PreAuthorize("hasRole('ADMIN')") // Garante que apenas administradores podem acessar
public class ControleRelatorios {

    private final PagamentoService pagamentoService;

    public ControleRelatorios(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/relatorios")
    public String relatorioMensalidades(@RequestParam(value = "status", required = false) String status, Model model) {
        
        List<Mensalidade> mensalidades = pagamentoService.buscarMensalidadesParaRelatorio(status);
        Map<String, BigDecimal> totais = pagamentoService.calcularTotaisMesCorrente();
        
        model.addAttribute("mensalidades", mensalidades);
        model.addAttribute("filtroAtual", status != null ? status.toUpperCase() : "TODAS");
        model.addAttribute("totalArrecadado", totais.get("totalArrecadado"));
        model.addAttribute("previsaoArrecadacao", totais.get("previsaoArrecadacao"));
        
        return "relatorios";
    }
}
