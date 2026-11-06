package com.nexus.user.application.usecase;

import com.nexus.common.core.FieldError;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;
import com.nexus.user.domain.service.RegistrationDetailsPolicy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// A person changes their own name and phone. E-mail, role, date of birth and the consents are not
// theirs to change here: a wrong date of birth would let a minor in, and the consents are a record
// of what they agreed to at sign-up. A blank phone clears it.
public class UpdateMyProfileUseCase {

    private static final int MAX_NAME_LENGTH = 255;

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    public UpdateMyProfileUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Transactional
    public ProfileResult update(String userId, String fullName, String phone) {
        User user = userRepositoryPort.findById(userId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));

        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException(List.of(new FieldError("fullName", "Full name is required")));
        }
        if (fullName.trim().length() > MAX_NAME_LENGTH) {
            throw new ValidationException(List.of(
                    new FieldError("fullName", "Full name must not exceed " + MAX_NAME_LENGTH + " characters")));
        }
        String normalizedPhone = RegistrationDetailsPolicy.normalizePhone(phone);

        User saved = userRepositoryPort.save(user.withFullName(fullName.trim()).withPhone(normalizedPhone));
        Role role = roleRepositoryPort.findById(saved.getRoleId().value())
                .orElseThrow(() -> new IllegalStateException("User references a non-existent role: " + saved.getRoleId()));
        return ProfileResult.from(saved, role.code());
    }
}
