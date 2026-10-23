package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.RatableTransactionRepositoryPort;
import com.nexus.user.application.port.out.RatingRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.*;
import com.nexus.common.core.exception.ConflictException;
import com.nexus.common.core.exception.ForbiddenException;
import com.nexus.common.core.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateTransactionUseCaseTest {

    private RatingRepositoryPort ratingRepositoryPort;
    private ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private UserRepositoryPort userRepositoryPort;
    private RatableTransactionRepositoryPort ratableTransactionRepositoryPort;
    private EventPublisherPort eventPublisherPort;
    private RateTransactionUseCase useCase;

    private User ratedUser() {
        return User.reconstitute("rated-1", "rated@example.com", "hash", "Rated User",
                new RoleId("role-buyer"), java.time.Instant.now(), null);
    }

    @BeforeEach
    void setUp() {
        ratingRepositoryPort = mock(RatingRepositoryPort.class);
        reputationProfileRepositoryPort = mock(ReputationProfileRepositoryPort.class);
        userRepositoryPort = mock(UserRepositoryPort.class);
        eventPublisherPort = mock(EventPublisherPort.class);
        ratableTransactionRepositoryPort = mock(RatableTransactionRepositoryPort.class);
        useCase = new RateTransactionUseCase(ratingRepositoryPort, reputationProfileRepositoryPort,
                userRepositoryPort, ratableTransactionRepositoryPort, eventPublisherPort);
        // The default fixture: order-1 was shipped by seller rated-1 to buyer rater-1.
        when(ratableTransactionRepositoryPort.find("order-1", TransactionType.ORDER)).thenReturn(Optional.of(
                new RatableTransaction("order-1", TransactionType.ORDER, "rater-1", "rated-1", java.time.Instant.now())));

        when(ratingRepositoryPort.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepositoryPort.findById("rated-1")).thenReturn(Optional.of(ratedUser()));
        when(reputationProfileRepositoryPort.findByUserId("rated-1")).thenReturn(Optional.empty());
    }

    @Test
    void rate_savesRatingAndUpdatesReputationProfile() {
        RatingResult result = useCase.rate("rater-1", "rated-1", "ORDER", "order-1", 5, "Great!");

        assertThat(result.score()).isEqualTo(5);
        assertThat(result.ratedUserId()).isEqualTo("rated-1");
        verify(reputationProfileRepositoryPort).save(argThat(p -> p.getScore() == 54));
        verify(eventPublisherPort).publish(any());
    }

    @Test
    void rate_rejectsRatingSelf() {
        assertThatThrownBy(() -> useCase.rate("user-1", "user-1", "ORDER", "order-1", 5, null))
                .isInstanceOf(ConflictException.class);
        verify(ratingRepositoryPort, never()).save(any());
    }

    @Test
    void rate_rejectsUnknownRatedUser() {
        when(userRepositoryPort.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.rate("rater-1", "unknown", "ORDER", "order-1", 5, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rate_rejectsDuplicateRatingForSameTransaction() {
        when(ratingRepositoryPort.existsByTransactionIdAndRaterId("order-1", "rater-1")).thenReturn(true);

        assertThatThrownBy(() -> useCase.rate("rater-1", "rated-1", "ORDER", "order-1", 5, null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rate_rejectsScoreOutOfRange() {
        assertThatThrownBy(() -> useCase.rate("rater-1", "rated-1", "ORDER", "order-1", 6, null))
                .isInstanceOf(com.nexus.common.core.exception.ValidationException.class);
    }

    @Test
    void rate_rejectsUnknownTransactionType() {
        assertThatThrownBy(() -> useCase.rate("rater-1", "rated-1", "SHIPMENT", "order-1", 5, null))
                .isInstanceOf(com.nexus.common.core.exception.ValidationException.class);
    }

    // ---- the transaction must really exist and be between these two users

    @Test
    void rate_rejectsAnInventedTransactionId() {
        // The hole this closes: any made-up id used to be accepted, so anyone could rate anyone.
        assertThatThrownBy(() -> useCase.rate("rater-1", "rated-1", "ORDER", "made-up-id", 1, "x"))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", "TRANSACTION_NOT_FOUND");

        verify(ratingRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    void rate_rejectsSomeoneWhoWasNotInTheTransaction() {
        assertThatThrownBy(() -> useCase.rate("stranger", "rated-1", "ORDER", "order-1", 1, "x"))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("errorCode", "NOT_A_PARTICIPANT");

        verify(ratingRepositoryPort, never()).save(any());
    }

    @Test
    void rate_rejectsRatingSomeoneWhoWasNotTheOtherParty() {
        // rater-1 really bought it, but the rated user is not the seller of that order
        when(userRepositoryPort.findById("someone-else")).thenReturn(Optional.of(ratedUser()));

        assertThatThrownBy(() -> useCase.rate("rater-1", "someone-else", "ORDER", "order-1", 1, "x"))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("errorCode", "NOT_A_PARTICIPANT");
    }

    @Test
    void rate_letsTheSellerRateTheBuyerToo() {
        when(userRepositoryPort.findById("rater-1")).thenReturn(Optional.of(ratedUser()));
        when(reputationProfileRepositoryPort.findByUserId("rater-1")).thenReturn(Optional.empty());

        RatingResult result = useCase.rate("rated-1", "rater-1", "ORDER", "order-1", 4, "Paid fast");

        assertThat(result.score()).isEqualTo(4);
        verify(ratingRepositoryPort).save(any(Rating.class));
    }

    @Test
    void rate_checksTheTypeToo_anOrderIdIsNotAnAuctionId() {
        // order-1 is only registered as an ORDER; the same id as an AUCTION does not exist
        assertThatThrownBy(() -> useCase.rate("rater-1", "rated-1", "AUCTION", "order-1", 5, "x"))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", "TRANSACTION_NOT_FOUND");
    }
}
