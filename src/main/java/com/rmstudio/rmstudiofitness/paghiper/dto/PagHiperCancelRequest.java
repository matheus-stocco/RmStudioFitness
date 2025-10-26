package com.rmstudio.rmstudiofitness.paghiper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa o corpo da requisição a ser enviada para a API do PagHiper
 * para cancelar uma transação PIX.
 */
public record PagHiperCancelRequest(
    @JsonProperty("token") String token,
    @JsonProperty("apiKey") String apiKey,
    @JsonProperty("status") String status,
    @JsonProperty("transaction_id") String transactionId
) {}






