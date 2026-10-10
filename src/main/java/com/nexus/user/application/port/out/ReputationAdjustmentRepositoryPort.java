package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.ReputationAdjustment;

import java.util.List;

public interface ReputationAdjustmentRepositoryPort {
    ReputationAdjustment save(ReputationAdjustment adjustment);
    List<ReputationAdjustment> findByUserId(String userId);
}
