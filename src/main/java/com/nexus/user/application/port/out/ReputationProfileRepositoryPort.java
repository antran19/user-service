package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.ReputationProfile;

import java.util.Optional;

public interface ReputationProfileRepositoryPort {
    Optional<ReputationProfile> findByUserId(String userId);
    ReputationProfile save(ReputationProfile profile);
}
