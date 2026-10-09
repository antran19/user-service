package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminChangeUserPasswordUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordHasherPort passwordHasherPort;
    private AdminChangeUserPasswordUseCase useCase;

    private User activeUser() {
        return User.reconstitute("user-1", "a@b.com", "old-hash", "Name", new RoleId("role-buyer"),
                Instant.now(), null);
    }

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        useCase = new AdminChangeUserPasswordUseCase(userRepositoryPort, passwordHasherPort);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordHasherPort.hash(any())).thenReturn("new-hash");
    }

    @Test
    void changePassword_setsNewHashWithoutNeedingTheOldPassword() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(activeUser()));

        useCase.changePassword("user-1", "longenough");

        verify(userRepositoryPort).save(argThat(u -> u.getHashedPassword().equals("new-hash")));
    }

    @Test
    void changePassword_rejectsUnknownUser() {
        when(userRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.changePassword("missing", "longenough"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void changePassword_rejectsWeakPassword() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(activeUser()));

        assertThatThrownBy(() -> useCase.changePassword("user-1", "short"))
                .isInstanceOf(ValidationException.class);
    }
}
