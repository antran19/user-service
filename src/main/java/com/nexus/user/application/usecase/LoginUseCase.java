package com.nexus.user.application.usecase;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.exception.InvalidCredentialsException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;

import java.util.List;

public class LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginUseCase(UserRepositoryPort userRepositoryPort,
                         RoleRepositoryPort roleRepositoryPort,
                         PasswordHasherPort passwordHasherPort,
                         JwtTokenProvider jwtTokenProvider) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResult login(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasherPort.matches(command.rawPassword(), user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        Role role = roleRepositoryPort.findById(user.getRoleId().value())
                .orElseThrow(() -> new IllegalStateException("User references a non-existent role: " + user.getRoleId()));

        String token = jwtTokenProvider.generateToken(user.getId(), role.code(), List.copyOf(role.privilegeCodes()));
        return new LoginResult(token, user.getId());
    }
}
