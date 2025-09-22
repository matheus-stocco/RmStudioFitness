package com.rmstudio.rmstudiofitness.paghiper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mapeia a resposta principal da API PagHiper ao criar uma transação.
 */
public record PagHiperResponse(
    @JsonProperty("pix_create_request") CreateRequest createRequest
) {
    public record CreateRequest(
        String result,
        @JsonProperty("response_message") String responseMessage,
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("pix_code") PagHiperPixCode pixCode
    ) {}
}
