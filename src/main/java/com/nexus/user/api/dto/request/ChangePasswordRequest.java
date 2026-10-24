package com.nexus.user.api.dto.request;

import com.nexus.user.api.validation.MaxUtf8Bytes;
import com.nexus.user.api.validation.PasswordConfirmation;
import com.nexus.user.api.validation.PasswordsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordsMatch(field = "confirmNewPassword")
public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") @MaxUtf8Bytes String newPassword,
        @NotBlank String confirmNewPassword) implements PasswordConfirmation {

    @Override
    public String passwordValue() {
        return newPassword;
    }

    @Override
    public String confirmationValue() {
        return confirmNewPassword;
    }
}
