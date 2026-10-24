package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.Email;
import com.nexus.user.api.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") @MaxUtf8Bytes String password,
        @NotBlank String fullName,
        @NotBlank String roleCode) {
}
