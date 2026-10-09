package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.application.exception.InvalidResetTokenException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.PasswordResetToken;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResetPasswordUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private PasswordHasherPort passwordHasherPort;
    private ResetPasswordUseCase useCase;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordResetTokenRepositoryPort = mock(PasswordResetTokenRepositoryPort.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        useCase = new ResetPasswordUseCase(userRepositoryPort, passwordResetTokenRepositoryPort, passwordHasherPort);
        existingUser = User.reconstitute("user-1", "carol@example.com", "hashed-old", "Carol Le",
                new RoleId("role-buyer"), Instant.now(), null);
    }

    @Test
    void resetPassword_updatesPasswordAndMarksTheTokenUsed() {
        when(passwordResetTokenRepositoryPort.findByTokenHash(PasswordResetToken.hash("raw-token")))
                .thenReturn(Optional.of(PasswordResetToken.issue("user-1", "raw-token")));
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(existingUser));
        when(passwordHasherPort.hash("newlongpassword")).thenReturn("hashed-new");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.resetPassword("raw-token", "newlongpassword");

        verify(userRepositoryPort).save(argThat(u -> u.getHashedPassword().equals("hashed-new")));
        verify(passwordResetTokenRepositoryPort).save(argThat(PasswordResetToken::isUsed));
    }

    @Test
    void resetPassword_throwsWhenTokenNotFound() {
        when(passwordResetTokenRepositoryPort.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.resetPassword("bad-token", "newlongpassword"))
                .isInstanceOf(InvalidResetTokenException.class);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void resetPassword_throwsWhenTokenExpired() {
        PasswordResetToken expired = PasswordResetToken.reconstitute("id-1", "user-1",
                PasswordResetToken.hash("raw-token"), Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.MINUTES), null);
        when(passwordResetTokenRepositoryPort.findByTokenHash(PasswordResetToken.hash("raw-token")))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> useCase.resetPassword("raw-token", "newlongpassword"))
                .isInstanceOf(InvalidResetTokenException.class);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void resetPassword_throwsWhenTokenAlreadyUsed() {
        PasswordResetToken used = PasswordResetToken.issue("user-1", "raw-token").markUsed();
        when(passwordResetTokenRepositoryPort.findByTokenHash(PasswordResetToken.hash("raw-token")))
                .thenReturn(Optional.of(used));

        assertThatThrownBy(() -> useCase.resetPassword("raw-token", "newlongpassword"))
                .isInstanceOf(InvalidResetTokenException.class);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void resetPassword_rejectsWeakNewPasswordWithoutConsumingTheToken() {
        PasswordResetToken resetToken = PasswordResetToken.issue("user-1", "raw-token");
        when(passwordResetTokenRepositoryPort.findByTokenHash(PasswordResetToken.hash("raw-token")))
                .thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> useCase.resetPassword("raw-token", "short"))
                .isInstanceOf(ValidationException.class);
        verify(userRepositoryPort, never()).save(any());
        verify(passwordResetTokenRepositoryPort, never()).save(any());
    }
}
