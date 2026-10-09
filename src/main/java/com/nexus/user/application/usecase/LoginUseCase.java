package com.nexus.user.application.usecase;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.exception.InvalidCredentialsException;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.ReputationProfile;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;

import java.util.List;

public class LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginUseCase(UserRepositoryPort userRepositoryPort,
                         RoleRepositoryPort roleRepositoryPort,
                         PasswordHasherPort passwordHasherPort,
                         ReputationProfileRepositoryPort reputationProfileRepositoryPort,
                         JwtTokenProvider jwtTokenProvider) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.reputationProfileRepositoryPort = reputationProfileRepositoryPort;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResult login(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        // A deleted account must behave exactly like a wrong password to the caller --
        // never reveal that the account existed/was removed.
        if (user.isDeleted()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordHasherPort.matches(command.rawPassword(), user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        Role role = roleRepositoryPort.findById(user.getRoleId().value())
                .orElseThrow(() -> new IllegalStateException("User references a non-existent role: " + user.getRoleId()));

        // A user with no ratings/penalties yet is the default neutral profile (TRUSTED) --
        // same lazy-default convention GetReputationUseCase uses, not a 404/error case.
        ReputationProfile reputation = reputationProfileRepositoryPort.findByUserId(user.getId())
                .orElseGet(() -> ReputationProfile.createDefault(user.getId()));

        String token = jwtTokenProvider.generateToken(user.getId(), role.code(),
                List.copyOf(role.privilegeCodes()), reputation.trustLevel().name());
        return new LoginResult(token, user.getId());
    }
}
