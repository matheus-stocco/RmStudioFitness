package com.rmstudio.rmstudiofitness.paghiper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PagHiperNotificationRequest(
    @JsonProperty("apiKey") String apiKey,
    @JsonProperty("transaction_id") String transactionId,
    @JsonProperty("notification_id") String notificationId,
    @JsonProperty("status") String status,
    @JsonProperty("order_id") String orderId // ID da nossa mensalidade
) {}
