package com.nexus.user.domain.model;

import java.time.Instant;

// A logged-out token's jti, kept only until the token itself would have expired naturally
// (see JwtAuthenticationFilter in common-security: it's checked on every request).
public record BlacklistedToken(String jti, Instant expiresAt) {
}
