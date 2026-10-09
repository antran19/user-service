package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ForgotPasswordUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private ForgotPasswordUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordResetTokenRepositoryPort = mock(PasswordResetTokenRepositoryPort.class);
        useCase = new ForgotPasswordUseCase(userRepositoryPort, passwordResetTokenRepositoryPort);
    }

    @Test
    void requestReset_savesATokenForAnExistingUserAndReturnsTheRawValue() {
        User user = User.reconstitute("user-1", "carol@example.com", "hashed", "Carol",
                new RoleId("role-buyer"), Instant.now(), null);
        when(userRepositoryPort.findByEmail("carol@example.com")).thenReturn(Optional.of(user));

        String rawToken = useCase.requestReset("carol@example.com");

        assertThat(rawToken).isNotBlank();
        verify(passwordResetTokenRepositoryPort)
                .save(argThat(t -> t.getUserId().equals("user-1") && t.matches(rawToken)));
    }

    @Test
    void requestReset_throwsWhenEmailDoesNotExist() {
        when(userRepositoryPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.requestReset("missing@example.com"))
                .isInstanceOf(NotFoundException.class);
        verify(passwordResetTokenRepositoryPort, never()).save(any());
    }

    @Test
    void requestReset_throwsWhenAccountIsDeleted() {
        User deletedUser = User.reconstitute("user-1", "carol@example.com", "hashed", "Carol",
                new RoleId("role-buyer"), Instant.now(), Instant.now());
        when(userRepositoryPort.findByEmail("carol@example.com")).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> useCase.requestReset("carol@example.com"))
                .isInstanceOf(NotFoundException.class);
        verify(passwordResetTokenRepositoryPort, never()).save(any());
    }
}
