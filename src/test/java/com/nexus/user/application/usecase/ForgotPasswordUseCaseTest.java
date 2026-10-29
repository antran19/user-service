package com.nexus.user.application.usecase;

import com.nexus.common.events.PasswordResetRequestedEvent;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.PasswordResetToken;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForgotPasswordUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private EventPublisherPort eventPublisherPort;
    private ForgotPasswordUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordResetTokenRepositoryPort = mock(PasswordResetTokenRepositoryPort.class);
        eventPublisherPort = mock(EventPublisherPort.class);
        useCase = new ForgotPasswordUseCase(userRepositoryPort, passwordResetTokenRepositoryPort, eventPublisherPort);
    }

    private User carol(Instant deletedAt) {
        return User.reconstitute("user-1", "carol@example.com", "hashed", "Carol",
                new RoleId("role-buyer"), Instant.now(), deletedAt);
    }

    @Test
    void requestReset_forAnExistingUser_publishesTheTokenInTheEvent_andStoresOnlyItsHash() {
        when(userRepositoryPort.findByEmail("carol@example.com")).thenReturn(Optional.of(carol(null)));

        useCase.requestReset("carol@example.com");

        ArgumentCaptor<PasswordResetRequestedEvent> event = ArgumentCaptor.forClass(PasswordResetRequestedEvent.class);
        verify(eventPublisherPort).publish(event.capture());
        String rawToken = event.getValue().getResetToken();
        assertThat(event.getValue().getUserId()).isEqualTo("user-1");
        assertThat(rawToken).isNotBlank();

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo("user-1");
        assertThat(saved.getValue().matches(rawToken)).isTrue();
        assertThat(saved.getValue().getTokenHash()).isNotEqualTo(rawToken);
    }

    @Test
    void requestReset_forAnUnknownEmail_doesNothing_andDoesNotThrow() {
        when(userRepositoryPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatCode(() -> useCase.requestReset("missing@example.com")).doesNotThrowAnyException();

        verify(passwordResetTokenRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    void requestReset_forADeletedAccount_doesNothing_andDoesNotThrow() {
        when(userRepositoryPort.findByEmail("carol@example.com")).thenReturn(Optional.of(carol(Instant.now())));

        assertThatCode(() -> useCase.requestReset("carol@example.com")).doesNotThrowAnyException();

        verify(passwordResetTokenRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }
}
