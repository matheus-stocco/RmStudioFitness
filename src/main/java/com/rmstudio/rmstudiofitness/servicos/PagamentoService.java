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

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;


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
    public List<Mensalidade> buscarMensalidadesParaRelatorio(String status, Integer ano, Integer mes) {
        List<Mensalidade> todasMensalidades = mensalidadeRepository.findAllWithDetails();
    
        // Filtro por ano e mês
        List<Mensalidade> mensalidadesFiltradas = todasMensalidades.stream()
            .filter(m -> {
                boolean anoMatch = (ano == null) || (m.getDataVencimento().getYear() == ano);
                boolean mesMatch = (mes == null) || (m.getDataVencimento().getMonthValue() == mes);
                return anoMatch && mesMatch;
            })
            .collect(Collectors.toList());
    
        if (status == null || status.trim().isEmpty() || "TODAS".equalsIgnoreCase(status)) {
            return mensalidadesFiltradas;
        }
    
        return mensalidadesFiltradas.stream()
            .filter(m -> {
                if ("ATRASADA".equalsIgnoreCase(status)) {
                    return "PENDENTE".equals(m.getStatus()) && m.getDataVencimento().isBefore(LocalDate.now());
                }
                return status.equalsIgnoreCase(m.getStatus());
            })
            .collect(Collectors.toList());
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
        return new PagHiperRequest(
            pagHiperService.getApiKey(),
            m.getId().toString(), // Usando o ID da mensalidade como order_id
            m.getPessoa().getEmail(),
            m.getPessoa().getNome(),
            m.getPessoa().getCpf().replaceAll("[^0-9]", ""), // A API espera apenas números
            m.getPessoa().getTelefone().replaceAll("[^0-9]", ""),
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
}
