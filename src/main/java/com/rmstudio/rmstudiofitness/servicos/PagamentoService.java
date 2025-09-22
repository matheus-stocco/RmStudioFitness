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


@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);
    private final PessoaRepository pessoaRepository;
    private final TipoPlanoRepository tipoPlanoRepository;
    private final MensalidadeRepository mensalidadeRepository;
    private final PagHiperService pagHiperService;
    private final ObjectMapper objectMapper;

    public PagamentoService(PessoaRepository pessoaRepository, TipoPlanoRepository tipoPlanoRepository, MensalidadeRepository mensalidadeRepository, PagHiperService pagHiperService, ObjectMapper objectMapper) {
        this.pessoaRepository = pessoaRepository;
        this.tipoPlanoRepository = tipoPlanoRepository;
        this.mensalidadeRepository = mensalidadeRepository;
        this.pagHiperService = pagHiperService;
        this.objectMapper = objectMapper;
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

        // 2. Cria a mensalidade inicial sem dados de PIX
        Mensalidade novaMensalidade = new Mensalidade();
        novaMensalidade.setPessoa(pessoa);
        novaMensalidade.setTipoPlano(tipoPlano);
        novaMensalidade.setValor(tipoPlano.getValor());
        novaMensalidade.setDataVencimento(LocalDate.now().plusDays(5));
        novaMensalidade.setStatus("PENDENTE");

        pessoa.getMensalidades().add(novaMensalidade);
        
        pessoaRepository.saveAndFlush(pessoa);
        return novaMensalidade;
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

    @Transactional
    public void processarNotificacao(PagHiperNotificationRequest notification) {
        logger.info("Recebendo notificação da PagHiper para a transação: {}", notification.transactionId());

        if (!"paid".equalsIgnoreCase(notification.status()) && !"approved".equalsIgnoreCase(notification.status())) {
            logger.warn("Status da notificação não é 'pago' ou 'aprovado'. Status: {}", notification.status());
            return; // Ignora notificações que não sejam de pagamento confirmado
        }

        Mensalidade mensalidade = mensalidadeRepository.findByTransactionId(notification.transactionId())
            .orElseThrow(() -> {
                logger.error("Mensalidade com transaction_id {} não encontrada.", notification.transactionId());
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensalidade não encontrada para esta transação.");
            });

        if ("PAGO".equals(mensalidade.getStatus())) {
            logger.info("Mensalidade {} já estava com status PAGO. Nenhuma alteração feita.", mensalidade.getId());
            return; // Evita processamento duplicado
        }

        mensalidade.setStatus("PAGO");
        mensalidade.setDataPagamento(LocalDate.now());
        mensalidadeRepository.save(mensalidade);

        logger.info("Mensalidade {} atualizada para PAGO com sucesso.", mensalidade.getId());
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
