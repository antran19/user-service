package com.nexus.user.application.port.out;

import java.time.Instant;

public interface BlacklistedTokenRepositoryPort {
    void blacklist(String jti, Instant expiresAt);
}
