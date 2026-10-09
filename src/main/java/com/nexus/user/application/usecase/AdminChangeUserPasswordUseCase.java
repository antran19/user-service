package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.User;
import com.nexus.user.domain.service.PasswordPolicy;
import org.springframework.transaction.annotation.Transactional;

// USER.CHANGE_PASSWORD: an admin setting another user's password directly (no old-password
// check -- that's the self-service PROFILE.CHANGE_PASSWORD flow, ChangePasswordUseCase).
public class AdminChangeUserPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public AdminChangeUserPasswordUseCase(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Transactional
    public void changePassword(String userId, String newRawPassword) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));
        if (user.isDeleted()) {
            throw new NotFoundException("USER_NOT_FOUND", "User not found: " + userId);
        }

        PasswordPolicy.validate(newRawPassword);
        String hashedPassword = passwordHasherPort.hash(newRawPassword);
        userRepositoryPort.save(user.withHashedPassword(hashedPassword));
    }
}
