package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.Rating;

public interface RatingRepositoryPort {
    Rating save(Rating rating);
    boolean existsByTransactionIdAndRaterId(String transactionId, String raterId);
}
