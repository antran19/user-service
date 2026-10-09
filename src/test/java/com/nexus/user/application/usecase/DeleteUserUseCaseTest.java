package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteUserUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private DeleteUserUseCase useCase;

    private User activeUser() {
        return User.reconstitute("user-1", "a@b.com", "hash", "Name", new RoleId("role-buyer"),
                Instant.now(), null);
    }

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        useCase = new DeleteUserUseCase(userRepositoryPort);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void delete_softDeletesTheUser() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(activeUser()));

        useCase.delete("user-1");

        verify(userRepositoryPort).save(argThat(User::isDeleted));
    }

    @Test
    void delete_rejectsUnknownUser() {
        when(userRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete("missing")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_rejectsAlreadyDeletedUser() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(activeUser().delete()));

        assertThatThrownBy(() -> useCase.delete("user-1")).isInstanceOf(NotFoundException.class);
    }
}
