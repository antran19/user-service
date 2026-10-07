package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.SellerRequest;
import com.nexus.user.domain.model.SellerRequestStatus;

import java.util.List;
import java.util.Optional;

public interface SellerRequestRepositoryPort {
    SellerRequest save(SellerRequest request);
    Optional<SellerRequest> findById(String id);
    Optional<SellerRequest> findPendingByUserId(String userId);
    List<SellerRequest> findByStatus(SellerRequestStatus status);
}
