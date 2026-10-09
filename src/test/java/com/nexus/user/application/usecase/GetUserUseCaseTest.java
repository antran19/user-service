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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private GetUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        useCase = new GetUserUseCase(userRepositoryPort, roleRepositoryPort);
    }

    @Test
    void get_returnsUserWithRoleCode() {
        User user = User.reconstitute("user-1", "a@b.com", "hash", "Name", new RoleId("role-buyer"),
                Instant.now(), null);
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(user));
        when(roleRepositoryPort.findById("role-buyer"))
                .thenReturn(Optional.of(new Role("role-buyer", "BUYER", "Buyer", Set.of())));

        UserResult result = useCase.get("user-1");

        assertThat(result.roleCode()).isEqualTo("BUYER");
    }

    @Test
    void get_rejectsDeletedUserAsNotFound() {
        User deleted = User.reconstitute("user-1", "a@b.com", "hash", "Name", new RoleId("role-buyer"),
                Instant.now(), Instant.now());
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> useCase.get("user-1")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void get_rejectsUnknownUser() {
        when(userRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.get("missing")).isInstanceOf(NotFoundException.class);
    }
}
