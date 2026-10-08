package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.ReputationPenaltyRepositoryPort;

import java.util.List;

public class ListReputationPenaltiesUseCase {

    private final ReputationPenaltyRepositoryPort reputationPenaltyRepositoryPort;

    public ListReputationPenaltiesUseCase(ReputationPenaltyRepositoryPort reputationPenaltyRepositoryPort) {
        this.reputationPenaltyRepositoryPort = reputationPenaltyRepositoryPort;
    }

    public List<PenaltyResult> list(String userId) {
        return reputationPenaltyRepositoryPort.findByUserId(userId).stream().map(PenaltyResult::from).toList();
    }
}
