package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CreateRoleRequest(@NotBlank String code, @NotBlank String name, Set<String> privilegeCodes) {
}
