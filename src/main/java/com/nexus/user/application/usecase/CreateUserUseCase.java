package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.events.UserRegisteredEvent;
import com.nexus.user.application.exception.DuplicateEmailException;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import com.nexus.user.domain.service.PasswordPolicy;
import org.springframework.transaction.annotation.Transactional;

// Admin-initiated account creation (USER.CREATE) -- distinct from self-service registration
// (RegisterUserUseCase), which always defaults to BUYER. Here the admin picks the role.
public class CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final EventPublisherPort eventPublisherPort;

    public CreateUserUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort,
                              PasswordHasherPort passwordHasherPort, EventPublisherPort eventPublisherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Transactional
    public UserResult create(String email, String rawPassword, String fullName, String roleCode) {
        PasswordPolicy.validate(rawPassword);

        if (userRepositoryPort.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException(email);
        }

        Role role = roleRepositoryPort.findByCode(roleCode)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found: " + roleCode));

        String hashedPassword = passwordHasherPort.hash(rawPassword);
        User user = User.register(email, hashedPassword, fullName, new RoleId(role.id()));
        User saved = userRepositoryPort.save(user);

        eventPublisherPort.publish(new UserRegisteredEvent(saved.getId(), saved.getEmail(), saved.getFullName()));

        return UserResult.from(saved, role.code());
    }
}
