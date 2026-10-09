package com.nexus.user.infrastructure.persistence;

import com.nexus.common.security.TokenBlacklistPort;
import com.nexus.user.application.port.out.BlacklistedTokenRepositoryPort;
import com.nexus.user.infrastructure.persistence.entity.BlacklistedTokenJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Implements both the application-layer write port (LogoutUseCase) and common-security's
// read port (JwtAuthenticationFilter) -- one bean, one table, no reason to split it.
@Component
public class BlacklistedTokenRepositoryAdapter implements BlacklistedTokenRepositoryPort, TokenBlacklistPort {

    private final BlacklistedTokenJpaRepository jpaRepository;

    public BlacklistedTokenRepositoryAdapter(BlacklistedTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void blacklist(String jti, Instant expiresAt) {
        jpaRepository.save(new BlacklistedTokenJpaEntity(jti, expiresAt));
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return jti != null && jpaRepository.existsById(jti);
    }
}
