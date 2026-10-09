package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
    PasswordResetToken save(PasswordResetToken resetToken);
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
