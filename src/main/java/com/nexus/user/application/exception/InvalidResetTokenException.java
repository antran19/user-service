package com.nexus.user.application.exception;

import com.nexus.common.core.exception.UnauthorizedException;

public class InvalidResetTokenException extends UnauthorizedException {
    public InvalidResetTokenException() {
        super("INVALID_RESET_TOKEN", "Reset token is invalid, expired, or already used");
    }
}
