package com.nexus.user.application.usecase;

import com.nexus.common.events.PasswordResetRequestedEvent;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.PasswordResetToken;
import com.nexus.user.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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

    // The raw token leaves the server in exactly one place: the PasswordResetRequested event,
    // which notification-service turns into an email to the account's owner. It is never
    // returned to the caller, or anyone who knows an address could reset that account's password.
    //
    // An unknown or deleted account does nothing, and the caller cannot tell the difference
    // (the endpoint answers the same either way), so the endpoint cannot be used to find out
    // which email addresses are registered.
    @Transactional
    public void requestReset(String email) {
        Optional<User> user = userRepositoryPort.findByEmail(email).filter(u -> !u.isDeleted());
        if (user.isEmpty()) {
            return;
        }

        String rawToken = UUID.randomUUID().toString();
        passwordResetTokenRepositoryPort.save(PasswordResetToken.issue(user.get().getId(), rawToken));
        eventPublisherPort.publish(new PasswordResetRequestedEvent(user.get().getId(), rawToken));
    }
}
