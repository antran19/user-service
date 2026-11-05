package com.nexus.user.application.usecase;

import com.nexus.common.events.UserRegisteredEvent;
import com.nexus.user.application.exception.DuplicateEmailException;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.RegistrationDetails;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import com.nexus.user.domain.service.PasswordPolicy;
import com.nexus.user.domain.service.RegistrationDetailsPolicy;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class RegisterUserUseCase {

    private static final String DEFAULT_ROLE_CODE = "BUYER";
    // "Is this person 18 today" is decided on the calendar of the users, not of the server.
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final EventPublisherPort eventPublisherPort;

    public RegisterUserUseCase(UserRepositoryPort userRepositoryPort,
                                RoleRepositoryPort roleRepositoryPort,
                                PasswordHasherPort passwordHasherPort,
                                EventPublisherPort eventPublisherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Transactional
    public UserRegistrationResult register(RegisterUserCommand command) {
        PasswordPolicy.validate(command.rawPassword());
        RegistrationDetails details = RegistrationDetailsPolicy.validate(command.phone(), command.birthDate(),
                command.termsAccepted(), command.escrowAcknowledged(), Instant.now(), LocalDate.now(VIETNAM));

        if (userRepositoryPort.findByEmail(command.email()).isPresent()) {
            throw new DuplicateEmailException(command.email());
        }

        Role defaultRole = roleRepositoryPort.findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role '" + DEFAULT_ROLE_CODE + "' is not seeded — check V2 migration"));

        String hashedPassword = passwordHasherPort.hash(command.rawPassword());
        User user = User.register(command.email(), hashedPassword, command.fullName(), new RoleId(defaultRole.id()), details);
        User saved = userRepositoryPort.save(user);

        eventPublisherPort.publish(new UserRegisteredEvent(saved.getId(), saved.getEmail(), saved.getFullName()));

        return new UserRegistrationResult(saved.getId(), saved.getEmail(), saved.getFullName());
    }
}
