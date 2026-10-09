package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(@NotBlank String fullName, @NotBlank String roleCode) {
}
