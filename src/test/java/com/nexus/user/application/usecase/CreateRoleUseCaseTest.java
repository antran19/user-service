package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ConflictException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateRoleUseCaseTest {

    private RoleRepositoryPort roleRepositoryPort;
    private CreateRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        useCase = new CreateRoleUseCase(roleRepositoryPort);
        when(roleRepositoryPort.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_savesNewRole() {
        when(roleRepositoryPort.findByCode("MODERATOR")).thenReturn(Optional.empty());

        RoleResult result = useCase.create("MODERATOR", "Moderator", Set.of("PRODUCT.VIEW"));

        assertThat(result.code()).isEqualTo("MODERATOR");
        assertThat(result.privilegeCodes()).containsExactly("PRODUCT.VIEW");
    }

    @Test
    void create_defaultsToEmptyPrivilegesWhenNoneGiven() {
        when(roleRepositoryPort.findByCode("MODERATOR")).thenReturn(Optional.empty());

        RoleResult result = useCase.create("MODERATOR", "Moderator", null);

        assertThat(result.privilegeCodes()).isEmpty();
    }

    @Test
    void create_rejectsDuplicateCode() {
        when(roleRepositoryPort.findByCode("ADMIN"))
                .thenReturn(Optional.of(new Role("id", "ADMIN", "Administrator", Set.of())));

        assertThatThrownBy(() -> useCase.create("ADMIN", "Dup", Set.of()))
                .isInstanceOf(ConflictException.class);
        verify(roleRepositoryPort, never()).save(any());
    }
}
