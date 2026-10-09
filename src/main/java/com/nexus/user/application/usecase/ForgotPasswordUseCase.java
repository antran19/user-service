package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.PasswordResetToken;
import com.nexus.user.domain.model.User;

import java.util.UUID;

public class ForgotPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;

    public ForgotPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                  PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordResetTokenRepositoryPort = passwordResetTokenRepositoryPort;
    }

    // Returns the raw token so the caller can hand it back for now (no real email channel
    // exists yet -- see HANDOFF.md). The stored copy is only ever the SHA-256 hash.
    public String requestReset(String email) {
        User user = userRepositoryPort.findByEmail(email)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new NotFoundException("EMAIL_NOT_FOUND", "No account found for email: " + email));

        String rawToken = UUID.randomUUID().toString();
        passwordResetTokenRepositoryPort.save(PasswordResetToken.issue(user.getId(), rawToken));
        return rawToken;
    }
}
