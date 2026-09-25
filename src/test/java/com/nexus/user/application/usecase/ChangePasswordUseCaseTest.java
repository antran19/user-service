package com.nexus.user.application.usecase;

import com.nexus.user.application.exception.InvalidCredentialsException;
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

class ChangePasswordUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordHasherPort passwordHasherPort;
    private ChangePasswordUseCase useCase;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        useCase = new ChangePasswordUseCase(userRepositoryPort, passwordHasherPort);
        existingUser = User.reconstitute("user-1", "carol@example.com", "hashed-old", "Carol Le",
                new RoleId("role-buyer"), Instant.now());
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(existingUser));
    }

    @Test
    void changePassword_savesNewHashWhenOldPasswordMatches() {
        when(passwordHasherPort.matches("oldpw", "hashed-old")).thenReturn(true);
        when(passwordHasherPort.hash("newlongpassword")).thenReturn("hashed-new");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.changePassword(new ChangePasswordCommand("user-1", "oldpw", "newlongpassword"));

        verify(userRepositoryPort).save(argThat(u -> u.getHashedPassword().equals("hashed-new")));
    }

    @Test
    void changePassword_rejectsWrongOldPassword() {
        when(passwordHasherPort.matches("wrong", "hashed-old")).thenReturn(false);

        assertThatThrownBy(() -> useCase.changePassword(new ChangePasswordCommand("user-1", "wrong", "newlongpassword")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepositoryPort, never()).save(any());
    }
}
