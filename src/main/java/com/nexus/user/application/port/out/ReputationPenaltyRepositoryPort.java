package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.ReputationPenalty;

import java.util.List;

public interface ReputationPenaltyRepositoryPort {
    ReputationPenalty save(ReputationPenalty penalty);
    boolean existsByReferenceIdAndReason(String referenceId, String reason);
    List<ReputationPenalty> findByUserId(String userId);
}
