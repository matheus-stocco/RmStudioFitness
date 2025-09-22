package com.rmstudio.rmstudiofitness.paghiper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mapeia os dados do PIX retornados pela API PagHiper.
 */
public record PagHiperPixCode(
    @JsonProperty("emv") String emv, // Este é o código "copia e cola"
    @JsonProperty("qrcode_image_url") String qrcodeImageUrl
) {}
