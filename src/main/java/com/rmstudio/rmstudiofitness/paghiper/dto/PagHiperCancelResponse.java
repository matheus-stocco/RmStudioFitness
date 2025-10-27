package com.rmstudio.rmstudiofitness.paghiper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mapeia a resposta da API PagHiper ao cancelar uma transação.
 */
public record PagHiperCancelResponse(
    @JsonProperty("cancellation_request") CancellationRequest cancellationRequest
) {
    public record CancellationRequest(
        String result,
        @JsonProperty("response_message") String responseMessage,
        @JsonProperty("http_code") String httpCode
    ) {}
}









