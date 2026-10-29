package com.nexus.user.api.dto.response;

// The same body for every address, registered or not: it says nothing about the account.
// The reset code itself is only ever sent by email.
public record ForgotPasswordResponse(String message) {

    public static final String GENERIC_MESSAGE =
            "If an account exists for that email, a password reset code has been sent to it.";

    public static ForgotPasswordResponse generic() {
        return new ForgotPasswordResponse(GENERIC_MESSAGE);
    }
}
