package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.exception.InvalidCredentialsException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.User;
import com.nexus.user.domain.service.PasswordPolicy;
import org.springframework.transaction.annotation.Transactional;

public class ChangePasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public ChangePasswordUseCase(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + command.userId()));

        if (!passwordHasherPort.matches(command.oldRawPassword(), user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        PasswordPolicy.validate(command.newRawPassword());
        String newHashedPassword = passwordHasherPort.hash(command.newRawPassword());
        userRepositoryPort.save(user.withHashedPassword(newHashedPassword));
    }
}
