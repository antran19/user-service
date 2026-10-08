package com.nexus.user.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Rating {

    private final String id;
    private final TransactionType transactionType;
    private final String transactionId;
    private final String raterId;
    private final String ratedUserId;
    private final int score;
    private final String comment;
    private final Instant createdAt;

    private Rating(String id, TransactionType transactionType, String transactionId, String raterId,
                    String ratedUserId, int score, String comment, Instant createdAt) {
        this.id = id;
        this.transactionType = transactionType;
        this.transactionId = transactionId;
        this.raterId = raterId;
        this.ratedUserId = ratedUserId;
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static Rating create(TransactionType transactionType, String transactionId, String raterId,
                                 String ratedUserId, int score, String comment) {
        return new Rating(UUID.randomUUID().toString(), transactionType, transactionId, raterId, ratedUserId,
                score, comment, Instant.now());
    }

    public static Rating reconstitute(String id, TransactionType transactionType, String transactionId,
                                       String raterId, String ratedUserId, int score, String comment,
                                       Instant createdAt) {
        return new Rating(id, transactionType, transactionId, raterId, ratedUserId, score, comment, createdAt);
    }

    public String getId() { return id; }
    public TransactionType getTransactionType() { return transactionType; }
    public String getTransactionId() { return transactionId; }
    public String getRaterId() { return raterId; }
    public String getRatedUserId() { return ratedUserId; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
