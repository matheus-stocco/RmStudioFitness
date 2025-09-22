package com.rmstudio.rmstudiofitness.paghiper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Representa o corpo da requisição a ser enviada para a API do PagHiper
 * para criar uma nova transação PIX.
 */
public record PagHiperRequest(
    @JsonProperty("apiKey") String apiKey,
    @JsonProperty("order_id") String orderId,
    @JsonProperty("payer_email") String payerEmail,
    @JsonProperty("payer_name") String payerName,
    @JsonProperty("payer_cpf_cnpj") String payerCpfCnpj,
    @JsonProperty("payer_phone") String payerPhone,
    @JsonProperty("notification_url") String notificationUrl,
    @JsonProperty("days_due_date") int daysDueDate,
    List<PagHiperItem> items
) {}
