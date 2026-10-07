package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.domain.model.SellerRequest;
import com.nexus.user.domain.model.SellerRequestStatus;
import com.nexus.user.infrastructure.persistence.entity.SellerRequestJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SellerRequestRepositoryAdapter implements SellerRequestRepositoryPort {

    private final SellerRequestJpaRepository jpaRepository;

    public SellerRequestRepositoryAdapter(SellerRequestJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SellerRequest save(SellerRequest request) {
        SellerRequestJpaEntity entity = new SellerRequestJpaEntity(
                UUID.fromString(request.getId()),
                UUID.fromString(request.getUserId()),
                request.getStatus().name(),
                request.getRequestedAt(),
                request.getReviewedAt(),
                request.getReviewedBy() == null ? null : UUID.fromString(request.getReviewedBy()));
        jpaRepository.save(entity);
        return request;
    }

    @Override
    public Optional<SellerRequest> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public Optional<SellerRequest> findPendingByUserId(String userId) {
        return jpaRepository.findByUserIdAndStatus(UUID.fromString(userId), SellerRequestStatus.PENDING.name())
                .map(this::toDomain);
    }

    @Override
    public List<SellerRequest> findByStatus(SellerRequestStatus status) {
        return jpaRepository.findByStatusOrderByRequestedAtAsc(status.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private SellerRequest toDomain(SellerRequestJpaEntity entity) {
        return SellerRequest.reconstitute(
                entity.getId().toString(),
                entity.getUserId().toString(),
                SellerRequestStatus.valueOf(entity.getStatus()),
                entity.getRequestedAt(),
                entity.getReviewedAt(),
                entity.getReviewedBy() == null ? null : entity.getReviewedBy().toString());
    }
}
