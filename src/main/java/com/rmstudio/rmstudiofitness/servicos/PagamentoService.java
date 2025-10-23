package com.rmstudio.rmstudiofitness.servicos;

import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.entidades.TipoPlano;
import com.rmstudio.rmstudiofitness.repositorios.MensalidadeRepository;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import com.rmstudio.rmstudiofitness.repositorios.TipoPlanoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmstudio.rmstudiofitness.paghiper.dto.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import com.rmstudio.rmstudiofitness.dtos.ResumoFinanceiroDTO;
import java.util.Comparator;
import java.util.LinkedHashMap;


@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);
    private final PessoaRepository pessoaRepository;
    private final TipoPlanoRepository tipoPlanoRepository;
    private final MensalidadeRepository mensalidadeRepository;
    private final PagHiperService pagHiperService;
    private final ObjectMapper objectMapper;
    private final AgendamentoService agendamentoService;

    public PagamentoService(PessoaRepository pessoaRepository, TipoPlanoRepository tipoPlanoRepository, MensalidadeRepository mensalidadeRepository, PagHiperService pagHiperService, ObjectMapper objectMapper, AgendamentoService agendamentoService) {
        this.pessoaRepository = pessoaRepository;
        this.tipoPlanoRepository = tipoPlanoRepository;
        this.mensalidadeRepository = mensalidadeRepository;
        this.pagHiperService = pagHiperService;
        this.objectMapper = objectMapper;
        this.agendamentoService = agendamentoService;
    }

    @Transactional
    public Mensalidade atribuirPlano(Long pessoaId, Long tipoPlanoId) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada."));
        
        TipoPlano tipoPlano = tipoPlanoRepository.findById(tipoPlanoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado."));

        // Se o usuário está trocando de plano, cancela as mensalidades pendentes do plano antigo.
        if (pessoa.getPlanoAtivo() != null) {
            pessoa.getMensalidades().stream()
                .filter(m -> "PENDENTE".equals(m.getStatus()))
                .forEach(m -> m.setStatus("CANCELADO"));
        }

        // 1. Atualiza o plano ativo da pessoa
        pessoa.setPlanoAtivo(tipoPlano);

        // 2. Gera a cobrança PROPORCIONAL do mês atual (com mínimo de 50%)
        LocalDate hoje = LocalDate.now();
        BigDecimal valorMensal = tipoPlano.getValorMensal();
        if (valorMensal == null) valorMensal = tipoPlano.getValor();

        int totalDiasMes = hoje.lengthOfMonth();
        int diasRestantesInclusivo = totalDiasMes - hoje.getDayOfMonth() + 1; // inclui o dia atual
        BigDecimal proporcao = BigDecimal.valueOf(diasRestantesInclusivo)
                .divide(BigDecimal.valueOf(totalDiasMes), 4, RoundingMode.HALF_UP);
        BigDecimal valorProporcional = valorMensal.multiply(proporcao).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorMinimo = valorMensal.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorPrimeira = valorProporcional.max(valorMinimo);

        boolean jaTemMensalidadeMesAtual = pessoa.getMensalidades().stream()
                .anyMatch(m -> "PENDENTE".equals(m.getStatus())
                        && m.getDataVencimento() != null
                        && m.getDataVencimento().getYear() == hoje.getYear()
                        && m.getDataVencimento().getMonthValue() == hoje.getMonthValue());

        Mensalidade mensalidadeAtual = null;
        if (!jaTemMensalidadeMesAtual) {
            mensalidadeAtual = new Mensalidade();
            mensalidadeAtual.setPessoa(pessoa);
            mensalidadeAtual.setTipoPlano(tipoPlano);
            mensalidadeAtual.setValor(valorPrimeira);
            // Vencimento: 5º dia útil do mês atual se ainda não passou; caso contrário, próximo dia útil a partir de hoje
            LocalDate quintoDiaAtual = agendamentoService.encontrarQuintoDiaUtilPublic(hoje.withDayOfMonth(1));
            LocalDate vencimentoAtual = hoje.isAfter(quintoDiaAtual) ? agendamentoService.ajustarParaProximoDiaUtil(hoje) : quintoDiaAtual;
            mensalidadeAtual.setDataVencimento(vencimentoAtual);
            mensalidadeAtual.setStatus("PENDENTE");
            pessoa.getMensalidades().add(mensalidadeAtual);
        }

        // 3. Gera a cobrança do PRÓXIMO mês (valor cheio) no 5º dia útil
        LocalDate proximoVencimento = agendamentoService.calcularProximoVencimento();
        boolean jaTemMensalidadeProximoMes = pessoa.getMensalidades().stream()
                .anyMatch(m -> "PENDENTE".equals(m.getStatus())
                        && m.getDataVencimento() != null
                        && m.getDataVencimento().getYear() == proximoVencimento.getYear()
                        && m.getDataVencimento().getMonthValue() == proximoVencimento.getMonthValue());

        Mensalidade mensalidadeProximoMes = null;
        if (!jaTemMensalidadeProximoMes) {
            mensalidadeProximoMes = new Mensalidade();
            mensalidadeProximoMes.setPessoa(pessoa);
            mensalidadeProximoMes.setTipoPlano(tipoPlano);
            mensalidadeProximoMes.setValor(valorMensal);
            mensalidadeProximoMes.setDataVencimento(proximoVencimento);
            mensalidadeProximoMes.setStatus("PENDENTE");
            pessoa.getMensalidades().add(mensalidadeProximoMes);
        }

        pessoaRepository.saveAndFlush(pessoa);
        // Retornamos a mensalidade do mês atual (quando criada); caso contrário, a do próximo mês
        return mensalidadeAtual != null ? mensalidadeAtual : mensalidadeProximoMes;
    }

    @Transactional
    public Mensalidade gerarPixParaMensalidade(Long mensalidadeId) {
        Mensalidade mensalidade = mensalidadeRepository.findByIdWithPessoa(mensalidadeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensalidade não encontrada."));

        // Se o PIX já foi gerado, apenas retorna os dados existentes
        if (mensalidade.getTransactionId() != null && !mensalidade.getTransactionId().isEmpty()) {
            logger.info("PIX já existente para a mensalidade {}. Retornando dados.", mensalidadeId);
            return mensalidade;
        }

        try {
            PagHiperRequest request = criarPagHiperRequest(mensalidade);
            String responseJson = pagHiperService.criarPix(request);
            
            PagHiperResponse response = objectMapper.readValue(responseJson, PagHiperResponse.class);
            
            if (response != null && response.createRequest() != null && "success".equals(response.createRequest().result())) {
                mensalidade.setTransactionId(response.createRequest().transactionId());
                mensalidade.setPixQrCodeUrl(response.createRequest().pixCode().qrcodeImageUrl());
                mensalidade.setPixCopiaECola(response.createRequest().pixCode().emv());
                return mensalidadeRepository.save(mensalidade);
            } else {
                throw new RuntimeException("Falha ao gerar PIX: Resposta inválida da API.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PIX para a mensalidade.", e);
        }
    }

    @Transactional(readOnly = true)
    public Mensalidade findMensalidadeParaPagamento(Long id) {
        return mensalidadeRepository.findByIdWithPessoaAndTipoPlano(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cobrança não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<Mensalidade> findMensalidadesByPessoaId(Long pessoaId) {
        return mensalidadeRepository.findByPessoaIdWithTipoPlano(pessoaId);
    }

    @Transactional(readOnly = true)
    public Page<Mensalidade> buscarMensalidadesParaRelatorio(String status, Integer ano, Integer mes, String alunoNome, Pageable pageable) {
        String statusFilter = (status == null || status.trim().isEmpty()) ? "TODAS" : status.toUpperCase();
        Page<Mensalidade> pageDeMensalidades = mensalidadeRepository.findForRelatorio(ano, mes, statusFilter, alunoNome, pageable);

        List<Long> ids = pageDeMensalidades.getContent().stream()
                .map(Mensalidade::getId)
                .collect(Collectors.toList());

        if (ids.isEmpty()){
            return pageDeMensalidades;
        }

        List<Mensalidade> mensalidadesCompletas = mensalidadeRepository.findAllWithDetailsByIds(ids);

        // O PageImpl espera que a lista de conteúdo já esteja na ordem correta, o que é garantido
        // porque ambas as consultas (nativa e JPQL) ordenam por data de vencimento.
        return new PageImpl<>(mensalidadesCompletas, pageDeMensalidades.getPageable(), pageDeMensalidades.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> calcularTotais(Integer ano, Integer mes) {
        LocalDate hoje = LocalDate.now();
        int anoAtual = (ano != null) ? ano : hoje.getYear();
        int mesAtual = (mes != null) ? mes : hoje.getMonthValue();

        LocalDate inicioDoMes = LocalDate.of(anoAtual, mesAtual, 1);
        LocalDate fimDoMes = inicioDoMes.withDayOfMonth(inicioDoMes.lengthOfMonth());

        List<Mensalidade> todasMensalidades = mensalidadeRepository.findAllWithDetails();

        BigDecimal totalArrecadado = todasMensalidades.stream()
            .filter(m -> "PAGO".equals(m.getStatus()) &&
                         m.getDataPagamento() != null &&
                         !m.getDataPagamento().isBefore(inicioDoMes) &&
                         !m.getDataPagamento().isAfter(fimDoMes))
            .map(Mensalidade::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal previsaoArrecadacao = todasMensalidades.stream()
            .filter(m -> {
                boolean isPendenteOuAtrasada = "PENDENTE".equals(m.getStatus());
                boolean isNoMesCorrente = !m.getDataVencimento().isBefore(inicioDoMes) && !m.getDataVencimento().isAfter(fimDoMes);
                return isPendenteOuAtrasada && isNoMesCorrente;
            })
            .map(Mensalidade::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
            "totalArrecadado", totalArrecadado,
            "previsaoArrecadacao", previsaoArrecadacao
        );
    }

    @Transactional
    public void processarNotificacao(PagHiperNotificationRequest notification) {
        logger.info("Recebendo notificação da PagHiper para a transação: {}", notification.transactionId());

        Mensalidade mensalidade = mensalidadeRepository.findByTransactionId(notification.transactionId())
            .orElseThrow(() -> {
                logger.error("Mensalidade com transaction_id {} não encontrada.", notification.transactionId());
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensalidade não encontrada para esta transação.");
            });

        String novoStatus = switch (notification.status().toLowerCase()) {
            case "paid", "approved" -> "PAGO";
            case "pending", "reserved" -> "PENDENTE";
            case "canceled", "refunded" -> "CANCELADO";
            default -> null;
        };

        if (novoStatus == null) {
            logger.warn("Status da notificação '{}' não mapeado. Nenhuma ação será tomada.", notification.status());
            return;
        }

        if (novoStatus.equals(mensalidade.getStatus())) {
            logger.info("Mensalidade {} já estava com status {}. Nenhuma alteração feita.", mensalidade.getId(), novoStatus);
            return; // Evita processamento duplicado
        }

        mensalidade.setStatus(novoStatus);
        if ("PAGO".equals(novoStatus)) {
            mensalidade.setDataPagamento(LocalDate.now());
        } else {
            mensalidade.setDataPagamento(null); // Garante que a data de pagamento seja nula se não estiver pago
        }

        mensalidadeRepository.save(mensalidade);
        logger.info("Mensalidade {} atualizada para {} com sucesso.", mensalidade.getId(), novoStatus);
    }

    @Transactional
    public void cancelarPlano(Long pessoaId) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada."));

        if (pessoa.getPlanoAtivo() == null) {
            throw new IllegalStateException("O usuário não possui um plano ativo para cancelar.");
        }

        // 1. Cancela todas as mensalidades com status PENDENTE
        List<Mensalidade> mensalidadesPendentes = pessoa.getMensalidades().stream()
            .filter(m -> "PENDENTE".equals(m.getStatus()))
            .collect(Collectors.toList());

        if (mensalidadesPendentes.isEmpty() && pessoa.getPlanoAtivo() != null) {
             logger.warn("Nenhuma mensalidade pendente encontrada para {}, mas o plano estava ativo. O plano será desativado.", pessoa.getNome());
        }

        for (Mensalidade m : mensalidadesPendentes) {
            m.setStatus("CANCELADO");
            logger.info("Mensalidade {} da pessoa {} cancelada.", m.getId(), pessoa.getNome());
        }

        // 2. Remove a associação do plano ativo da pessoa
        pessoa.setPlanoAtivo(null);

        // 3. Salva as alterações
        pessoaRepository.save(pessoa);
    }

    private PagHiperRequest criarPagHiperRequest(Mensalidade m) {
        // Validação para CPF nulo
        String cpf = m.getPessoa().getCpf();
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new RuntimeException("CPF é obrigatório para gerar PIX. Por favor, cadastre o CPF da pessoa antes de gerar o pagamento.");
        }
        
        // Validação para telefone nulo
        String telefone = m.getPessoa().getTelefone();
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new RuntimeException("Telefone é obrigatório para gerar PIX. Por favor, cadastre o telefone da pessoa antes de gerar o pagamento.");
        }
        
        return new PagHiperRequest(
            pagHiperService.getApiKey(),
            m.getId().toString(), // Usando o ID da mensalidade como order_id
            m.getPessoa().getEmail(),
            m.getPessoa().getNome(),
            cpf.replaceAll("[^0-9]", ""), // A API espera apenas números
            telefone.replaceAll("[^0-9]", ""),
            pagHiperService.getNotificationUrl(),
            5,
            Collections.singletonList(new PagHiperItem(
                m.getTipoPlano().getNome(),
                1,
                m.getTipoPlano().getId().toString(),
                m.getValor().multiply(new java.math.BigDecimal(100)).intValue() // Convertendo para centavos
            ))
        );
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroDTO calcularResumoFinanceiroAnual(Integer ano) {
        int anoCalculo = (ano != null) ? ano : LocalDate.now().getYear();
        List<Mensalidade> mensalidades = mensalidadeRepository.findByYearWithDetails(anoCalculo);
        LocalDate hoje = LocalDate.now();

        // 1. Total Arrecadado (Apenas mensalidades PAGAS no ano)
        BigDecimal totalArrecadado = mensalidades.stream()
            .filter(m -> "PAGO".equals(m.getStatus()))
            .map(Mensalidade::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Contagem por Status
        long pagas = mensalidades.stream().filter(m -> "PAGO".equals(m.getStatus())).count();
        long pendentes = mensalidades.stream().filter(m -> "PENDENTE".equals(m.getStatus()) && !m.getDataVencimento().isBefore(hoje)).count();
        long atrasadas = mensalidades.stream().filter(m -> "PENDENTE".equals(m.getStatus()) && m.getDataVencimento().isBefore(hoje)).count();
        long canceladas = mensalidades.stream().filter(m -> "CANCELADO".equals(m.getStatus())).count();

        // 3. Faturamento por Plano
        // Garante que todos os planos sejam listados, mesmo que com valor zero.
        Map<String, BigDecimal> faturamentoPorPlano = tipoPlanoRepository.findAll().stream()
            .collect(Collectors.toMap(
                TipoPlano::getNome,
                p -> BigDecimal.ZERO
            ));

        mensalidades.stream()
            .filter(m -> "PAGO".equals(m.getStatus()))
            .forEach(m -> faturamentoPorPlano.merge(m.getTipoPlano().getNome(), m.getValor(), BigDecimal::add));

        // Ordena o mapa final por valor (decrescente) e depois por nome (crescente para desempate)
        Map<String, BigDecimal> faturamentoOrdenado = faturamentoPorPlano.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        // 4. Defasagem por Plano (valores em atraso)
        Map<String, BigDecimal> defasagemPorPlano = tipoPlanoRepository.findAll().stream()
            .collect(Collectors.toMap(
                TipoPlano::getNome,
                p -> BigDecimal.ZERO
            ));

        mensalidades.stream()
            .filter(m -> "PENDENTE".equals(m.getStatus()) && m.getDataVencimento().isBefore(hoje))
            .forEach(m -> defasagemPorPlano.merge(m.getTipoPlano().getNome(), m.getValor(), BigDecimal::add));

        Map<String, BigDecimal> defasagemOrdenada = defasagemPorPlano.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        // 5. Arrecadação Mensal (para o gráfico)
        Map<Integer, BigDecimal> arrecadacaoMensal = mensalidades.stream()
            .filter(m -> "PAGO".equals(m.getStatus()))
            .collect(Collectors.groupingBy(
                m -> m.getDataPagamento().getMonthValue(),
                Collectors.mapping(Mensalidade::getValor, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
        
        // Garante que todos os 12 meses estão presentes no mapa para o gráfico
        for (int i = 1; i <= 12; i++) {
            arrecadacaoMensal.putIfAbsent(i, BigDecimal.ZERO);
        }

        ResumoFinanceiroDTO dto = new ResumoFinanceiroDTO();
        dto.setAno(anoCalculo);
        dto.setTotalArrecadado(totalArrecadado);
        dto.setTotalMensalidadesPagas(pagas);
        dto.setTotalMensalidadesPendentes(pendentes);
        dto.setTotalMensalidadesAtrasadas(atrasadas);
        dto.setTotalMensalidadesCanceladas(canceladas);
        dto.setFaturamentoPorPlano(faturamentoOrdenado);
        dto.setDefasagemPorPlano(defasagemOrdenada);
        dto.setArrecadacaoMensal(arrecadacaoMensal);
        
        // Média mensal baseada nos meses que já passaram ou no mês atual
        int mesesConsiderados = (anoCalculo < hoje.getYear()) ? 12 : hoje.getMonthValue();
        dto.setMediaMensal(
            totalArrecadado.divide(BigDecimal.valueOf(mesesConsiderados), 2, RoundingMode.HALF_UP)
        );

        return dto;
    }
}
