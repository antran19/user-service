package com.nexus.user.application.exception;

import com.nexus.common.core.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String email) {
        super("EMAIL_ALREADY_REGISTERED", "Email already registered: " + email);
    }
}
