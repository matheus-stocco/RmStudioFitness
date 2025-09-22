package com.rmstudio.rmstudiofitness.paghiper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa um item dentro de uma transação do PagHiper.
 * Utiliza a anotação @JsonProperty para mapear os campos Java (camelCase)
 * para os campos do JSON da API (snake_case).
 */
public record PagHiperItem(
    String description,
    int quantity,
    @JsonProperty("item_id") String itemId,
    @JsonProperty("price_cents") int priceCents
) {}
