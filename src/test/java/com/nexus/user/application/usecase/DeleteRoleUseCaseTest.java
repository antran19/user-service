package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ConflictException;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeleteRoleUseCaseTest {

    private RoleRepositoryPort roleRepositoryPort;
    private UserRepositoryPort userRepositoryPort;
    private DeleteRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        userRepositoryPort = mock(UserRepositoryPort.class);
        useCase = new DeleteRoleUseCase(roleRepositoryPort, userRepositoryPort);
        when(roleRepositoryPort.findById("role-1"))
                .thenReturn(Optional.of(new Role("role-1", "MODERATOR", "Moderator", Set.of())));
    }

    @Test
    void delete_removesRoleNotInUse() {
        when(userRepositoryPort.existsByRoleId("role-1")).thenReturn(false);

        useCase.delete("role-1");

        verify(roleRepositoryPort).deleteById("role-1");
    }

    @Test
    void delete_rejectsRoleStillAssignedToUsers() {
        when(userRepositoryPort.existsByRoleId("role-1")).thenReturn(true);

        assertThatThrownBy(() -> useCase.delete("role-1")).isInstanceOf(ConflictException.class);
        verify(roleRepositoryPort, never()).deleteById(any());
    }

    @Test
    void delete_rejectsUnknownRole() {
        when(roleRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete("missing")).isInstanceOf(NotFoundException.class);
    }
}
