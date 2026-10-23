package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.RatableTransactionRepositoryPort;
import com.nexus.user.application.port.out.RatingRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.RatableTransaction;
import com.nexus.user.domain.model.Rating;
import com.nexus.user.domain.model.ReputationProfile;
import com.nexus.user.domain.model.TransactionType;
import com.nexus.common.core.FieldError;
import com.nexus.common.core.exception.ConflictException;
import com.nexus.common.core.exception.ForbiddenException;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.core.exception.ValidationException;
import com.nexus.common.events.UserRatedEvent;

import java.util.List;
import java.util.Locale;

public class RateTransactionUseCase {

    private final RatingRepositoryPort ratingRepositoryPort;
    private final ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RatableTransactionRepositoryPort ratableTransactionRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    public RateTransactionUseCase(RatingRepositoryPort ratingRepositoryPort,
                                   ReputationProfileRepositoryPort reputationProfileRepositoryPort,
                                   UserRepositoryPort userRepositoryPort,
                                   RatableTransactionRepositoryPort ratableTransactionRepositoryPort,
                                   EventPublisherPort eventPublisherPort) {
        this.ratingRepositoryPort = ratingRepositoryPort;
        this.reputationProfileRepositoryPort = reputationProfileRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.ratableTransactionRepositoryPort = ratableTransactionRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    public RatingResult rate(String raterId, String ratedUserId, String rawTransactionType,
                              String transactionId, int score, String comment) {
        TransactionType transactionType = parseTransactionType(rawTransactionType);
        if (score < 1 || score > 5) {
            throw new ValidationException(List.of(new FieldError("score", "score must be between 1 and 5")));
        }
        if (raterId.equals(ratedUserId)) {
            throw new ConflictException("CANNOT_RATE_SELF", "You cannot rate yourself");
        }
        userRepositoryPort.findById(ratedUserId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + ratedUserId));
        // The transaction must exist (a seller has shipped it) and the two users must be its buyer and
        // seller. Without this anyone could rate anyone with an invented transactionId.
        RatableTransaction transaction = ratableTransactionRepositoryPort.find(transactionId, transactionType)
                .orElseThrow(() -> new NotFoundException("TRANSACTION_NOT_FOUND",
                        "No shipped " + transactionType.name().toLowerCase(Locale.ROOT) + " with id " + transactionId));
        if (!transaction.isBetween(raterId, ratedUserId)) {
            throw new ForbiddenException("NOT_A_PARTICIPANT",
                    "You can only rate the other party of a transaction you took part in");
        }
        if (ratingRepositoryPort.existsByTransactionIdAndRaterId(transactionId, raterId)) {
            throw new ConflictException("RATING_ALREADY_SUBMITTED",
                    "You have already rated this transaction");
        }

        Rating rating = Rating.create(transactionType, transactionId, raterId, ratedUserId, score, comment);
        Rating saved = ratingRepositoryPort.save(rating);

        ReputationProfile profile = reputationProfileRepositoryPort.findByUserId(ratedUserId)
                .orElseGet(() -> ReputationProfile.createDefault(ratedUserId));
        reputationProfileRepositoryPort.save(profile.applyRating(score));

        eventPublisherPort.publish(new UserRatedEvent(saved.getId(), raterId, ratedUserId,
                transactionType.name(), transactionId, score));
        return RatingResult.from(saved);
    }

    private static TransactionType parseTransactionType(String raw) {
        try {
            return TransactionType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidationException(List.of(new FieldError("transactionType",
                    "transactionType must be one of ORDER, AUCTION")));
        }
    }
}
