package com.nexus.user.domain.service;

import com.nexus.common.core.FieldError;
import com.nexus.common.core.exception.ValidationException;

import java.util.List;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new ValidationException(List.of(
                    new FieldError("password", "Password must be at least " + MIN_LENGTH + " characters")));
        }
    }
}
