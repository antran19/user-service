package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.events.PasswordResetRequestedEvent;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.PasswordResetToken;
import com.nexus.user.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ForgotPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    public ForgotPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                  PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
                                  EventPublisherPort eventPublisherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordResetTokenRepositoryPort = passwordResetTokenRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    // Returns the raw token too, so a caller with no other way to see it (manual
    // testing, or today's frontend) still can -- but the real delivery channel is now
    // the published event, which notification-service turns into an actual email.
    @Transactional
    public String requestReset(String email) {
        User user = userRepositoryPort.findByEmail(email)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new NotFoundException("EMAIL_NOT_FOUND", "No account found for email: " + email));

        String rawToken = UUID.randomUUID().toString();
        passwordResetTokenRepositoryPort.save(PasswordResetToken.issue(user.getId(), rawToken));
        eventPublisherPort.publish(new PasswordResetRequestedEvent(user.getId(), rawToken));
        return rawToken;
    }
}
