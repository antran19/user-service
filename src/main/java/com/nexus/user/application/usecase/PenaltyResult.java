package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.ReputationPenalty;

import java.time.Instant;

public record PenaltyResult(String id, String userId, String reason, int points, String referenceId,
                             Instant appliedAt) {

    public static PenaltyResult from(ReputationPenalty penalty) {
        return new PenaltyResult(penalty.getId(), penalty.getUserId(), penalty.getReason(), penalty.getPoints(),
                penalty.getReferenceId(), penalty.getAppliedAt());
    }
}
