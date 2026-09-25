package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.OutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxJpaEntity, UUID> {
    List<OutboxJpaEntity> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
