package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdjustReputationRequest(@NotNull Integer delta, @NotBlank String reason) {
}
