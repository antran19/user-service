package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.ReputationAdjustment;

import java.time.Instant;

public record AdjustmentResult(String id, String userId, String adminId, int delta, String reason,
                                Instant appliedAt) {

    public static AdjustmentResult from(ReputationAdjustment adjustment) {
        return new AdjustmentResult(adjustment.getId(), adjustment.getUserId(), adjustment.getAdminId(),
                adjustment.getDelta(), adjustment.getReason(), adjustment.getAppliedAt());
    }
}
