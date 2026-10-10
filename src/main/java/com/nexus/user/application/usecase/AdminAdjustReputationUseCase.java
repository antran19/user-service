package com.nexus.user.application.usecase;

import com.nexus.common.core.FieldError;
import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.application.port.out.ReputationAdjustmentRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.domain.model.ReputationAdjustment;
import com.nexus.user.domain.model.ReputationProfile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// USER.REPUTATION.ADJUST (SRS privilege table, Admin only): a manual override, separate
// from the automatic AUCTION_PAYMENT_TIMEOUT penalty path.
public class AdminAdjustReputationUseCase {

    private final ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private final ReputationAdjustmentRepositoryPort reputationAdjustmentRepositoryPort;

    public AdminAdjustReputationUseCase(ReputationProfileRepositoryPort reputationProfileRepositoryPort,
                                         ReputationAdjustmentRepositoryPort reputationAdjustmentRepositoryPort) {
        this.reputationProfileRepositoryPort = reputationProfileRepositoryPort;
        this.reputationAdjustmentRepositoryPort = reputationAdjustmentRepositoryPort;
    }

    @Transactional
    public ReputationResult adjust(String userId, String adminId, int delta, String reason) {
        if (delta == 0) {
            throw new ValidationException(List.of(new FieldError("delta", "delta must not be zero")));
        }
        if (reason == null || reason.isBlank()) {
            throw new ValidationException(List.of(new FieldError("reason", "reason must not be blank")));
        }

        ReputationProfile profile = reputationProfileRepositoryPort.findByUserId(userId)
                .orElseGet(() -> ReputationProfile.createDefault(userId));
        ReputationProfile updated = reputationProfileRepositoryPort.save(profile.adjust(delta));

        reputationAdjustmentRepositoryPort.save(ReputationAdjustment.create(userId, adminId, delta, reason));

        return ReputationResult.from(updated);
    }
}
