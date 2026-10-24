package com.nexus.user.api.dto.request;

import com.nexus.user.api.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// An admin sets the password directly (no repeat field), but the same length rule applies.
public record AdminChangePasswordRequest(
        @NotBlank @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") @MaxUtf8Bytes String newPassword) {
}
