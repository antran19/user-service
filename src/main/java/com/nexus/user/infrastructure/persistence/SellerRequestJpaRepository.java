package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.SellerRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerRequestJpaRepository extends JpaRepository<SellerRequestJpaEntity, UUID> {
    Optional<SellerRequestJpaEntity> findByUserIdAndStatus(UUID userId, String status);
    List<SellerRequestJpaEntity> findByStatusOrderByRequestedAtAsc(String status);
}
