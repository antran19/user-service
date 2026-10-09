package com.nexus.user.application.usecase;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.port.out.BlacklistedTokenRepositoryPort;
import io.jsonwebtoken.Claims;

public class LogoutUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final BlacklistedTokenRepositoryPort blacklistedTokenRepositoryPort;

    public LogoutUseCase(JwtTokenProvider jwtTokenProvider, BlacklistedTokenRepositoryPort blacklistedTokenRepositoryPort) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.blacklistedTokenRepositoryPort = blacklistedTokenRepositoryPort;
    }

    public void logout(String rawToken) {
        Claims claims = jwtTokenProvider.parseClaims(rawToken);
        String jti = claims.get("jti", String.class);
        if (jti == null) {
            return;
        }
        blacklistedTokenRepositoryPort.blacklist(jti, claims.getExpiration().toInstant());
    }
}
