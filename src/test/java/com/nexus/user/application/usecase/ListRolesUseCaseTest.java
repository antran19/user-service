package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListRolesUseCaseTest {

    @Test
    void list_returnsAllRoles() {
        RoleRepositoryPort roleRepositoryPort = mock(RoleRepositoryPort.class);
        when(roleRepositoryPort.findAll()).thenReturn(List.of(new Role("id", "BUYER", "Buyer", Set.of())));

        List<RoleResult> results = new ListRolesUseCase(roleRepositoryPort).list();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).code()).isEqualTo("BUYER");
    }
}
