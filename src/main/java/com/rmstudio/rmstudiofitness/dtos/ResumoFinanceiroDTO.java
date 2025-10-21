package com.rmstudio.rmstudiofitness.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ResumoFinanceiroDTO {
    private int ano;
    private BigDecimal totalArrecadado;
    private BigDecimal mediaMensal;
    private long totalMensalidadesPagas;
    private long totalMensalidadesPendentes;
    private long totalMensalidadesAtrasadas;
    private long totalMensalidadesCanceladas;
    private Map<String, BigDecimal> faturamentoPorPlano;
    private Map<String, BigDecimal> defasagemPorPlano;
    private Map<String, Long> contagemStatus;
    private Map<Integer, BigDecimal> arrecadacaoMensal; // Chave: Mês (1-12), Valor: Total
}
