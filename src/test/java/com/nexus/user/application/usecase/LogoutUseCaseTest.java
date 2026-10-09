package com.nexus.user.application.usecase;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.port.out.BlacklistedTokenRepositoryPort;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogoutUseCaseTest {

    private JwtTokenProvider jwtTokenProvider;
    private BlacklistedTokenRepositoryPort blacklistedTokenRepositoryPort;
    private LogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        blacklistedTokenRepositoryPort = mock(BlacklistedTokenRepositoryPort.class);
        useCase = new LogoutUseCase(jwtTokenProvider, blacklistedTokenRepositoryPort);
    }

    @Test
    void logout_blacklistsTheTokensJtiUntilItsOriginalExpiry() {
        Claims claims = mock(Claims.class);
        Instant expiresAt = Instant.now().plusSeconds(3600).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        when(claims.get("jti", String.class)).thenReturn("jti-1");
        when(claims.getExpiration()).thenReturn(Date.from(expiresAt));
        when(jwtTokenProvider.parseClaims("raw-token")).thenReturn(claims);

        useCase.logout("raw-token");

        verify(blacklistedTokenRepositoryPort).blacklist("jti-1", expiresAt);
    }

    @Test
    void logout_doesNothingForATokenWithNoJti() {
        // Simulates a token minted before the jti claim existed -- same fail-open
        // convention used for other claims an old token might be missing.
        Claims claims = mock(Claims.class);
        when(claims.get("jti", String.class)).thenReturn(null);
        when(jwtTokenProvider.parseClaims("raw-token")).thenReturn(claims);

        useCase.logout("raw-token");

        verify(blacklistedTokenRepositoryPort, never()).blacklist(any(), any());
    }
}
