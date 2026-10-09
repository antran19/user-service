package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetRoleUseCaseTest {

    @Test
    void get_returnsRole() {
        RoleRepositoryPort roleRepositoryPort = mock(RoleRepositoryPort.class);
        when(roleRepositoryPort.findById("role-1")).thenReturn(Optional.of(new Role("role-1", "BUYER", "Buyer", Set.of())));

        RoleResult result = new GetRoleUseCase(roleRepositoryPort).get("role-1");

        assertThat(result.code()).isEqualTo("BUYER");
    }

    @Test
    void get_rejectsUnknownRole() {
        RoleRepositoryPort roleRepositoryPort = mock(RoleRepositoryPort.class);
        when(roleRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetRoleUseCase(roleRepositoryPort).get("missing"))
                .isInstanceOf(NotFoundException.class);
    }
}
