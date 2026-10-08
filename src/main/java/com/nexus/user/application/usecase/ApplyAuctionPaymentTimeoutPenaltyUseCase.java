package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.ReputationPenaltyRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.domain.model.ReputationPenalty;
import com.nexus.user.domain.model.ReputationProfile;
import com.nexus.common.events.ReputationPenaltyAppliedEvent;

// Triggered by consuming AuctionPaymentTimeoutEvent off the auction-events Kafka topic:
// auction-service already detects when an auction winner fails to pay within the deadline,
// but previously nothing acted on that -- it was only recorded for audit by
// notification-service. This is the SRS's "Payment Timeout Penalty" (PAYMENT_TIMEOUT_PENALTY_SCORE
// = 10) actually taking effect.
public class ApplyAuctionPaymentTimeoutPenaltyUseCase {

    public static final String REASON = "AUCTION_PAYMENT_TIMEOUT";
    private static final int PENALTY_POINTS = 10;

    private final ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private final ReputationPenaltyRepositoryPort reputationPenaltyRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    public ApplyAuctionPaymentTimeoutPenaltyUseCase(ReputationProfileRepositoryPort reputationProfileRepositoryPort,
                                                      ReputationPenaltyRepositoryPort reputationPenaltyRepositoryPort,
                                                      EventPublisherPort eventPublisherPort) {
        this.reputationProfileRepositoryPort = reputationProfileRepositoryPort;
        this.reputationPenaltyRepositoryPort = reputationPenaltyRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    // Idempotent: Kafka is at-least-once, so a redelivered AuctionPaymentTimeoutEvent for an
    // auction already penalized must be a no-op, not a double penalty.
    public void apply(String userId, String auctionId) {
        if (reputationPenaltyRepositoryPort.existsByReferenceIdAndReason(auctionId, REASON)) {
            return;
        }

        ReputationPenalty penalty = ReputationPenalty.create(userId, REASON, PENALTY_POINTS, auctionId);
        reputationPenaltyRepositoryPort.save(penalty);

        ReputationProfile profile = reputationProfileRepositoryPort.findByUserId(userId)
                .orElseGet(() -> ReputationProfile.createDefault(userId));
        reputationProfileRepositoryPort.save(profile.applyPenalty(PENALTY_POINTS));

        eventPublisherPort.publish(new ReputationPenaltyAppliedEvent(userId, REASON, PENALTY_POINTS, auctionId));
    }
}
