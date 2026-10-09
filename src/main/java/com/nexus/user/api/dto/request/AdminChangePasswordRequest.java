package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminChangePasswordRequest(@NotBlank String newPassword) {
}
