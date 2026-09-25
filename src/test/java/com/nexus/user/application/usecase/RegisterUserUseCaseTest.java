package com.nexus.user.application.usecase;

import com.nexus.common.events.UserRegisteredEvent;
import com.nexus.user.application.exception.DuplicateEmailException;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private PasswordHasherPort passwordHasherPort;
    private EventPublisherPort eventPublisherPort;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        eventPublisherPort = mock(EventPublisherPort.class);
        useCase = new RegisterUserUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort, eventPublisherPort);

        when(roleRepositoryPort.findByCode("BUYER"))
                .thenReturn(Optional.of(new Role("role-buyer", "BUYER", "Buyer", Set.of("AUTH.LOGIN"))));
        when(passwordHasherPort.hash("longenough")).thenReturn("hashed-longenough");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void register_savesNewUserWithDefaultBuyerRole() {
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        UserRegistrationResult result = useCase.register(
                new RegisterUserCommand("alice@example.com", "longenough", "Alice Nguyen"));

        assertThat(result.email()).isEqualTo("alice@example.com");
        assertThat(result.fullName()).isEqualTo("Alice Nguyen");
        verify(userRepositoryPort).save(argThat(u ->
                u.getEmail().equals("alice@example.com")
                        && u.getHashedPassword().equals("hashed-longenough")
                        && u.getRoleId().value().equals("role-buyer")));
        verify(eventPublisherPort).publish(argThat(event ->
                event instanceof UserRegisteredEvent registered
                        && registered.getEmail().equals("alice@example.com")));
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepositoryPort.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> useCase.register(
                new RegisterUserCommand("alice@example.com", "longenough", "Alice Nguyen")))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepositoryPort, never()).save(any());
    }
}
