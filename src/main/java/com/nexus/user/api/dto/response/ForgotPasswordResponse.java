package com.nexus.user.api.dto.response;

// resetToken is returned directly here as a temporary stand-in for a real email channel,
// which doesn't exist yet (see HANDOFF.md). Once notification-service can actually send
// email, this should carry a generic "if that email exists, check your inbox" message
// instead, and the token should only ever leave the server inside that email.
public record ForgotPasswordResponse(String resetToken) {
}
