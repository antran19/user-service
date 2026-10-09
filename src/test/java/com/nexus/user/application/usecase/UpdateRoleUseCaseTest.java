package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateRoleUseCaseTest {

    private RoleRepositoryPort roleRepositoryPort;
    private UpdateRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        useCase = new UpdateRoleUseCase(roleRepositoryPort);
        when(roleRepositoryPort.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void update_replacesNameAndPrivileges() {
        when(roleRepositoryPort.findById("role-1"))
                .thenReturn(Optional.of(new Role("role-1", "MODERATOR", "Old Name", Set.of("PRODUCT.VIEW"))));

        RoleResult result = useCase.update("role-1", "New Name", Set.of("PRODUCT.VIEW", "PRODUCT.LIST"));

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.privilegeCodes()).containsExactlyInAnyOrder("PRODUCT.VIEW", "PRODUCT.LIST");
    }

    @Test
    void update_rejectsUnknownRole() {
        when(roleRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update("missing", "X", Set.of())).isInstanceOf(NotFoundException.class);
    }
}
