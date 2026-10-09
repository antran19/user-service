package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

public class DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public DeleteUserUseCase(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Transactional
    public void delete(String userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));
        if (user.isDeleted()) {
            throw new NotFoundException("USER_NOT_FOUND", "User not found: " + userId);
        }
        userRepositoryPort.save(user.delete());
    }
}
