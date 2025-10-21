package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import com.rmstudio.rmstudiofitness.servicos.PagamentoService;
import com.rmstudio.rmstudiofitness.repositorios.MensalidadeRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.LocalDate;


@Controller
@PreAuthorize("hasRole('ADMIN')") // Garante que apenas administradores podem acessar
public class ControleRelatorios {

    private final PagamentoService pagamentoService;
    private final PessoaRepository pessoaRepository;

    public ControleRelatorios(PagamentoService pagamentoService, PessoaRepository pessoaRepository, MensalidadeRepository mensalidadeRepository) {
        this.pagamentoService = pagamentoService;
        this.pessoaRepository = pessoaRepository;
    }

    @GetMapping("/relatorios/mensalidades")
    public String relatorioMensalidades(
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "ano", required = false) Integer ano,
        @RequestParam(value = "mes", required = false) Integer mes,
        @PageableDefault(size = 15, sort = "dataVencimento", direction = Sort.Direction.DESC) Pageable pageable,
        Model model
    ) {

        Page<Mensalidade> mensalidades = pagamentoService.buscarMensalidadesParaRelatorio(status, ano, mes, pageable);
        Map<String, BigDecimal> totais = pagamentoService.calcularTotais(ano, mes);
        
        model.addAttribute("mensalidades", mensalidades);
        model.addAttribute("filtroAtual", status != null ? status.toUpperCase() : "TODAS");
        model.addAttribute("totalArrecadado", totais.get("totalArrecadado"));
        model.addAttribute("previsaoArrecadacao", totais.get("previsaoArrecadacao"));
        model.addAttribute("anoSelecionado", ano != null ? ano : LocalDate.now().getYear());
        model.addAttribute("mesSelecionado", mes != null ? mes : LocalDate.now().getMonthValue());

        // Para popular os filtros de ano e mês na view
        List<Integer> anos = IntStream.rangeClosed(2023, LocalDate.now().getYear() + 1).boxed().collect(Collectors.toList());
        model.addAttribute("anos", anos);
        
        return "relatorios/relatorio-mensalidades";
    }

    @GetMapping("/relatorios/membros")
    public String relatorioMembros(@RequestParam(value = "nome", required = false) String nome,
                                   @RequestParam(value = "status", required = false, defaultValue = "TODOS") String status,
                                   @PageableDefault(size = 15, sort = "nome") Pageable pageable,
                                   Model model) {
        Page<Pessoa> membros;
        boolean hasNome = nome != null && !nome.trim().isEmpty();

        switch (status.toUpperCase()) {
            case "ATIVO":
                membros = hasNome
                    ? pessoaRepository.findByNomeContainingIgnoreCaseAndPlanoAtivoIsNotNull(nome, pageable)
                    : pessoaRepository.findByPlanoAtivoIsNotNullOrderByNome(pageable);
                break;
            case "OCIOSO":
                membros = hasNome
                    ? pessoaRepository.findByNomeContainingIgnoreCaseAndPlanoAtivoIsNull(nome, pageable)
                    : pessoaRepository.findByPlanoAtivoIsNullOrderByNome(pageable);
                break;
            default: // "TODOS"
                membros = hasNome
                    ? pessoaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    : pessoaRepository.findAllByOrderByNome(pageable);
                break;
        }

        // Novas estatísticas
        long totalAtivos = pessoaRepository.countByPlanoAtivoIsNotNull();
        long totalOciosos = pessoaRepository.countByPlanoAtivoIsNull();
        long totalMembros = totalAtivos + totalOciosos; // Calculando o total a partir dos status
        long totalMasculino = pessoaRepository.countByGenero("M");
        long totalFeminino = pessoaRepository.countByGenero("F");
        long totalOutro = pessoaRepository.countByGenero("O");

        model.addAttribute("totalMembros", totalMembros);
        model.addAttribute("totalAtivos", totalAtivos);
        model.addAttribute("totalOciosos", totalOciosos);
        model.addAttribute("totalMasculino", totalMasculino);
        model.addAttribute("totalFeminino", totalFeminino);
        model.addAttribute("totalOutro", totalOutro);

        model.addAttribute("membros", membros);
        model.addAttribute("nomePesquisado", nome);
        model.addAttribute("filtroStatus", status.toUpperCase());
        return "relatorios/relatorio-membros";
    }
}
