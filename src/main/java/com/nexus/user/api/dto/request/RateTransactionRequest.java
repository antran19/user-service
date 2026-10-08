package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RateTransactionRequest(
        @NotBlank String ratedUserId,
        @NotBlank String transactionType,
        @NotBlank String transactionId,
        @Min(1) @Max(5) int score,
        String comment) {
}
