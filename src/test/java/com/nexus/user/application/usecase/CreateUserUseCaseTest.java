package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.core.exception.ValidationException;
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

class CreateUserUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private PasswordHasherPort passwordHasherPort;
    private EventPublisherPort eventPublisherPort;
    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        eventPublisherPort = mock(EventPublisherPort.class);
        useCase = new CreateUserUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort, eventPublisherPort);

        when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());
        when(roleRepositoryPort.findByCode("SELLER"))
                .thenReturn(Optional.of(new Role("role-seller", "SELLER", "Seller", Set.of())));
        when(passwordHasherPort.hash(any())).thenReturn("hashed");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_savesUserWithGivenRoleAndPublishesEvent() {
        UserResult result = useCase.create("new@example.com", "longenough", "New User", "SELLER");

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.roleCode()).isEqualTo("SELLER");
        verify(eventPublisherPort).publish(any());
    }

    @Test
    void create_rejectsDuplicateEmail() {
        when(userRepositoryPort.findByEmail("dup@example.com"))
                .thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> useCase.create("dup@example.com", "longenough", "X", "SELLER"))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void create_rejectsUnknownRole() {
        when(roleRepositoryPort.findByCode("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create("new@example.com", "longenough", "X", "GHOST"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_rejectsWeakPassword() {
        assertThatThrownBy(() -> useCase.create("new@example.com", "short", "X", "SELLER"))
                .isInstanceOf(ValidationException.class);
    }
}
