package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.domain.model.PasswordResetToken;
import com.nexus.user.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken resetToken) {
        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity(
                UUID.fromString(resetToken.getId()), UUID.fromString(resetToken.getUserId()),
                resetToken.getTokenHash(), resetToken.getCreatedAt(), resetToken.getExpiresAt(),
                resetToken.getUsedAt());
        jpaRepository.save(entity);
        return resetToken;
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return PasswordResetToken.reconstitute(entity.getId().toString(), entity.getUserId().toString(),
                entity.getTokenHash(), entity.getCreatedAt(), entity.getExpiresAt(), entity.getUsedAt());
    }
}
