package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListUsersUseCaseTest {

    @Test
    void list_returnsNonDeletedUsersWithRoleCodes() {
        UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
        RoleRepositoryPort roleRepositoryPort = mock(RoleRepositoryPort.class);

        User user = User.reconstitute("user-1", "a@b.com", "hash", "Name", new RoleId("role-buyer"),
                Instant.now(), null);
        when(userRepositoryPort.findAllNotDeleted()).thenReturn(List.of(user));
        when(roleRepositoryPort.findAll()).thenReturn(List.of(new Role("role-buyer", "BUYER", "Buyer", Set.of())));

        List<UserResult> results = new ListUsersUseCase(userRepositoryPort, roleRepositoryPort).list();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).roleCode()).isEqualTo("BUYER");
    }
}
