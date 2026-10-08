package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.ReputationPenaltyRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.domain.model.ReputationPenalty;
import com.nexus.user.domain.model.ReputationProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplyAuctionPaymentTimeoutPenaltyUseCaseTest {

    private ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private ReputationPenaltyRepositoryPort reputationPenaltyRepositoryPort;
    private EventPublisherPort eventPublisherPort;
    private ApplyAuctionPaymentTimeoutPenaltyUseCase useCase;

    @BeforeEach
    void setUp() {
        reputationProfileRepositoryPort = mock(ReputationProfileRepositoryPort.class);
        reputationPenaltyRepositoryPort = mock(ReputationPenaltyRepositoryPort.class);
        eventPublisherPort = mock(EventPublisherPort.class);
        useCase = new ApplyAuctionPaymentTimeoutPenaltyUseCase(reputationProfileRepositoryPort,
                reputationPenaltyRepositoryPort, eventPublisherPort);
        when(reputationPenaltyRepositoryPort.save(any(ReputationPenalty.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(reputationProfileRepositoryPort.save(any(ReputationProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void apply_deductsTenPointsAndRecordsPenalty() {
        when(reputationProfileRepositoryPort.findByUserId("winner-1")).thenReturn(Optional.empty());
        when(reputationPenaltyRepositoryPort.existsByReferenceIdAndReason("auction-1",
                ApplyAuctionPaymentTimeoutPenaltyUseCase.REASON)).thenReturn(false);

        useCase.apply("winner-1", "auction-1");

        verify(reputationProfileRepositoryPort).save(argThat(p -> p.getScore() == 40));
        verify(reputationPenaltyRepositoryPort).save(argThat(p ->
                p.getUserId().equals("winner-1") && p.getPoints() == 10 && p.getReferenceId().equals("auction-1")));
        verify(eventPublisherPort).publish(any());
    }

    @Test
    void apply_isIdempotentWhenAlreadyPenalizedForThisAuction() {
        when(reputationPenaltyRepositoryPort.existsByReferenceIdAndReason("auction-1",
                ApplyAuctionPaymentTimeoutPenaltyUseCase.REASON)).thenReturn(true);

        useCase.apply("winner-1", "auction-1");

        verify(reputationPenaltyRepositoryPort, never()).save(any());
        verify(reputationProfileRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }
}
