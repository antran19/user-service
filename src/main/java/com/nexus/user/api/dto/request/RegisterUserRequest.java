package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String fullName) {
}
