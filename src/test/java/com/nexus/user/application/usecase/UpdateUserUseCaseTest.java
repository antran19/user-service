package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateUserUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private UpdateUserUseCase useCase;

    private User activeUser() {
        return User.reconstitute("user-1", "a@b.com", "hash", "Old Name", new RoleId("role-buyer"),
                Instant.now(), null);
    }

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        useCase = new UpdateUserUseCase(userRepositoryPort, roleRepositoryPort);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roleRepositoryPort.findByCode("SELLER"))
                .thenReturn(Optional.of(new Role("role-seller", "SELLER", "Seller", Set.of())));
    }

    @Test
    void update_changesNameAndRole() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(activeUser()));

        UserResult result = useCase.update("user-1", "New Name", "SELLER");

        assertThat(result.fullName()).isEqualTo("New Name");
        assertThat(result.roleCode()).isEqualTo("SELLER");
    }

    @Test
    void update_rejectsUnknownUser() {
        when(userRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update("missing", "X", "SELLER")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_rejectsDeletedUserAsNotFound() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(activeUser().delete()));

        assertThatThrownBy(() -> useCase.update("user-1", "X", "SELLER")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_rejectsUnknownRole() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(activeUser()));
        when(roleRepositoryPort.findByCode("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update("user-1", "X", "GHOST")).isInstanceOf(NotFoundException.class);
    }
}
