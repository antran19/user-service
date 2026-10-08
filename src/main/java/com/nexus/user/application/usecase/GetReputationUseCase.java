package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.domain.model.ReputationProfile;

public class GetReputationUseCase {

    private final ReputationProfileRepositoryPort reputationProfileRepositoryPort;

    public GetReputationUseCase(ReputationProfileRepositoryPort reputationProfileRepositoryPort) {
        this.reputationProfileRepositoryPort = reputationProfileRepositoryPort;
    }

    // A user with no ratings/penalties yet simply has the default neutral profile -- not a 404.
    public ReputationResult getReputation(String userId) {
        ReputationProfile profile = reputationProfileRepositoryPort.findByUserId(userId)
                .orElseGet(() -> ReputationProfile.createDefault(userId));
        return ReputationResult.from(profile);
    }
}
