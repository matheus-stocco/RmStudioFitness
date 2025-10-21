package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import com.rmstudio.rmstudiofitness.servicos.PagamentoService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.LocalDate;
import com.rmstudio.rmstudiofitness.servicos.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import com.lowagie.text.DocumentException;
import org.springframework.http.HttpStatus;
import com.rmstudio.rmstudiofitness.dtos.ResumoFinanceiroDTO;


@Controller
@PreAuthorize("hasRole('ADMIN')") // Garante que apenas administradores podem acessar
public class ControleRelatorios {

    private final PagamentoService pagamentoService;
    private final PessoaRepository pessoaRepository;
    private final PdfService pdfService;

    public ControleRelatorios(PagamentoService pagamentoService, PessoaRepository pessoaRepository, PdfService pdfService) {
        this.pagamentoService = pagamentoService;
        this.pessoaRepository = pessoaRepository;
        this.pdfService = pdfService;
    }

    @GetMapping("/relatorios/mensalidades")
    public String relatorioMensalidades(
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "ano", required = false) Integer ano,
        @RequestParam(value = "mes", required = false) Integer mes,
        @RequestParam(value = "alunoNome", required = false) String alunoNome,
        @PageableDefault(size = 15) Pageable pageable,
        Model model
    ) {

        Pageable pageableSemOrdenacao = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Mensalidade> mensalidades = pagamentoService.buscarMensalidadesParaRelatorio(status, ano, mes, alunoNome, pageableSemOrdenacao);
        Map<String, BigDecimal> totais = pagamentoService.calcularTotais(ano, mes);
        
        model.addAttribute("mensalidades", mensalidades);
        model.addAttribute("filtroAtual", status != null ? status.toUpperCase() : "TODAS");
        model.addAttribute("totalArrecadado", totais.get("totalArrecadado"));
        model.addAttribute("previsaoArrecadacao", totais.get("previsaoArrecadacao"));
        model.addAttribute("anoSelecionado", ano != null ? ano : LocalDate.now().getYear());
        model.addAttribute("mesSelecionado", mes != null ? mes : LocalDate.now().getMonthValue());
        model.addAttribute("alunoNomePesquisado", alunoNome);

        // Se a busca retornar mensalidades de apenas um aluno, disponibiliza-o para o botão de PDF
        if (alunoNome != null && !alunoNome.isBlank()) {
            List<Pessoa> alunosEncontrados = mensalidades.getContent().stream()
                .map(Mensalidade::getPessoa)
                .distinct()
                .toList();
            
            if (alunosEncontrados.size() == 1) {
                model.addAttribute("alunoUnico", alunosEncontrados.get(0));
            }
        }
        
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
                    : pessoaRepository.findByPlanoAtivoIsNotNull(pageable);
                break;
            case "OCIOSO":
                membros = hasNome
                    ? pessoaRepository.findByNomeContainingIgnoreCaseAndPlanoAtivoIsNull(nome, pageable)
                    : pessoaRepository.findByPlanoAtivoIsNull(pageable);
                break;
            default: // "TODOS"
                membros = hasNome
                    ? pessoaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    : pessoaRepository.findAll(pageable);
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

    @GetMapping("/relatorios/membros/pdf")
    public ResponseEntity<byte[]> gerarRelatorioMembrosPdf(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "status", required = false, defaultValue = "TODOS") String status,
            @RequestParam(value = "sort", defaultValue = "nome,asc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Order order = new Sort.Order(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        List<Pessoa> membros = pessoaRepository.findAll(Sort.by(order)); // Exemplo simplificado, precisaria implementar a lógica de filtro completa

        // Lógica de filtro (simplificada para o exemplo, idealmente seria refatorada)
        final String finalStatus = status.toUpperCase();
        List<Pessoa> membrosFiltrados = membros.stream()
                .filter(p -> {
                    boolean matchStatus = "TODOS".equals(finalStatus) ||
                                          ("ATIVO".equals(finalStatus) && p.getPlanoAtivo() != null) ||
                                          ("OCIOSO".equals(finalStatus) && p.getPlanoAtivo() == null);
                    boolean matchNome = (nome == null || nome.trim().isEmpty()) ||
                                        p.getNome().toLowerCase().contains(nome.toLowerCase());
                    return matchStatus && matchNome;
                })
                .collect(Collectors.toList());

        long totalAtivos = pessoaRepository.countByPlanoAtivoIsNotNull();
        long totalOciosos = pessoaRepository.countByPlanoAtivoIsNull();
        
        Map<String, Object> dados = new HashMap<>();
        dados.put("membros", membrosFiltrados);
        dados.put("totalMembros", totalAtivos + totalOciosos);
        dados.put("totalAtivos", totalAtivos);
        dados.put("totalOciosos", totalOciosos);
        dados.put("totalMasculino", pessoaRepository.countByGenero("M"));
        dados.put("totalFeminino", pessoaRepository.countByGenero("F"));
        dados.put("totalOutro", pessoaRepository.countByGenero("O"));
        dados.put("dataGeracao", LocalDateTime.now());

        try {
            byte[] pdfBytes = pdfService.gerarPdfDeHtml("relatorios/relatorio-membros-pdf", dados);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "relatorio-membros-" + LocalDate.now() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException | DocumentException e) {
            // Log do erro
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/relatorios/mensalidades/pdf")
    public ResponseEntity<byte[]> gerarRelatorioFinanceiroPdf(
        @RequestParam(value = "ano", required = false) Integer ano) {

        ResumoFinanceiroDTO resumo = pagamentoService.calcularResumoFinanceiroAnual(ano);
        
        Map<String, Object> dados = new HashMap<>();
        dados.put("resumo", resumo);
        dados.put("dataGeracao", LocalDateTime.now());

        try {
            byte[] pdfBytes = pdfService.gerarPdfDeHtml("relatorios/relatorio-financeiro-pdf", dados);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "relatorio-financeiro-" + resumo.getAno() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException | DocumentException e) {
            // Log do erro e
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/relatorios/mensalidades/aluno/{alunoId}/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> gerarRelatorioAlunoPdf(@PathVariable Long alunoId) {
        Pessoa aluno = pessoaRepository.findByIdWithDetails(alunoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));

        List<Mensalidade> mensalidades = pagamentoService.findMensalidadesByPessoaId(alunoId);

        Map<String, Object> dados = new HashMap<>();
        dados.put("aluno", aluno);
        dados.put("mensalidades", mensalidades);
        dados.put("dataGeracao", LocalDateTime.now());

        try {
            byte[] pdfBytes = pdfService.gerarPdfDeHtml("relatorios/relatorio-mensalidades-aluno-pdf", dados);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "relatorio-mensalidades-" + aluno.getNome().replaceAll("\\s+", "-") + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
