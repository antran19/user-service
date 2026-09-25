package com.nexus.user.application.usecase;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.exception.InvalidCredentialsException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private PasswordHasherPort passwordHasherPort;
    private JwtTokenProvider jwtTokenProvider;
    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        useCase = new LoginUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort, jwtTokenProvider);
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        User user = User.reconstitute("user-1", "alice@example.com", "hashed-pw", "Alice Nguyen",
                new RoleId("role-buyer"), java.time.Instant.now());
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("longenough", "hashed-pw")).thenReturn(true);
        when(roleRepositoryPort.findById("role-buyer"))
                .thenReturn(Optional.of(new Role("role-buyer", "BUYER", "Buyer", Set.of("AUTH.LOGIN"))));
        when(jwtTokenProvider.generateToken(eq("user-1"), eq("BUYER"), anyList())).thenReturn("signed-jwt");

        LoginResult result = useCase.login(new LoginCommand("alice@example.com", "longenough"));

        assertThat(result.token()).isEqualTo("signed-jwt");
        assertThat(result.userId()).isEqualTo("user-1");
    }

    @Test
    void login_rejectsUnknownEmail() {
        when(userRepositoryPort.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.login(new LoginCommand("nobody@example.com", "whatever")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_rejectsWrongPassword() {
        User user = User.reconstitute("user-1", "alice@example.com", "hashed-pw", "Alice Nguyen",
                new RoleId("role-buyer"), java.time.Instant.now());
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrong", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> useCase.login(new LoginCommand("alice@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
