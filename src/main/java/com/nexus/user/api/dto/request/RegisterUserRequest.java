package com.nexus.user.api.dto.request;

import com.nexus.user.api.validation.MaxUtf8Bytes;
import com.nexus.user.api.validation.PasswordConfirmation;
import com.nexus.user.api.validation.PasswordsMatch;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// 72 is BCrypt's limit: it only uses the first 72 bytes of a password.
// phone and birthDate are optional but checked when given (RegistrationDetailsPolicy); the two
// consents are required. birthDate is an ISO date: 2000-12-31.
@PasswordsMatch
public record RegisterUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") @MaxUtf8Bytes String password,
        @NotBlank String confirmPassword,
        @NotBlank @Size(max = 255) String fullName,
        @Size(max = 20) String phone,
        LocalDate birthDate,
        @NotNull(message = "The terms of service must be accepted")
        @AssertTrue(message = "The terms of service must be accepted") Boolean termsAccepted,
        @NotNull(message = "The escrow rules must be acknowledged")
        @AssertTrue(message = "The escrow rules must be acknowledged") Boolean escrowAcknowledged) implements PasswordConfirmation {

    @Override
    public String passwordValue() {
        return password;
    }

    @Override
    public String confirmationValue() {
        return confirmPassword;
    }
}
