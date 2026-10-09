package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.exception.InvalidResetTokenException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.PasswordResetToken;
import com.nexus.user.domain.model.User;
import com.nexus.user.domain.service.PasswordPolicy;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public class ResetPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public ResetPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                 PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
                                 PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordResetTokenRepositoryPort = passwordResetTokenRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Transactional
    public void resetPassword(String rawToken, String newRawPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepositoryPort
                .findByTokenHash(PasswordResetToken.hash(rawToken))
                .orElseThrow(InvalidResetTokenException::new);

        if (!resetToken.isValid(rawToken, Instant.now())) {
            throw new InvalidResetTokenException();
        }

        // Validate before touching the user/token rows: a client-side password-policy
        // failure must not burn a legitimate one-time token.
        PasswordPolicy.validate(newRawPassword);

        User user = userRepositoryPort.findById(resetToken.getUserId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + resetToken.getUserId()));

        userRepositoryPort.save(user.withHashedPassword(passwordHasherPort.hash(newRawPassword)));
        passwordResetTokenRepositoryPort.save(resetToken.markUsed());
    }
}
