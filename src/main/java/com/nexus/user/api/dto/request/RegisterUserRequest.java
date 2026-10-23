package com.nexus.user.api.dto.request;

import com.nexus.user.api.validation.PasswordConfirmation;
import com.nexus.user.api.validation.PasswordsMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 72 is BCrypt's limit: it only uses the first 72 bytes of a password.
@PasswordsMatch
public record RegisterUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") String password,
        @NotBlank String confirmPassword,
        @NotBlank @Size(max = 255) String fullName) implements PasswordConfirmation {

    @Override
    public String passwordValue() {
        return password;
    }

    @Override
    public String confirmationValue() {
        return confirmPassword;
    }
}
