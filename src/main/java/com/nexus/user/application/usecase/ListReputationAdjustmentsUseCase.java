package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.ReputationAdjustmentRepositoryPort;

import java.util.List;

public class ListReputationAdjustmentsUseCase {

    private final ReputationAdjustmentRepositoryPort reputationAdjustmentRepositoryPort;

    public ListReputationAdjustmentsUseCase(ReputationAdjustmentRepositoryPort reputationAdjustmentRepositoryPort) {
        this.reputationAdjustmentRepositoryPort = reputationAdjustmentRepositoryPort;
    }

    public List<AdjustmentResult> list(String userId) {
        return reputationAdjustmentRepositoryPort.findByUserId(userId).stream().map(AdjustmentResult::from).toList();
    }
}
