package com.nexus.user.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

public class PasswordResetToken {

    // The raw token has high entropy (random UUID), so unlike a user-chosen password it
    // doesn't need bcrypt-style slow hashing -- a deterministic digest lets the use case
    // look the row up by hash directly instead of scanning every unexpired row.
    private static final Duration TTL = Duration.ofMinutes(30);

    private final String id;
    private final String userId;
    private final String tokenHash;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Instant usedAt;

    private PasswordResetToken(String id, String userId, String tokenHash, Instant createdAt, Instant expiresAt,
                                Instant usedAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public static PasswordResetToken issue(String userId, String rawToken) {
        Instant now = Instant.now();
        return new PasswordResetToken(UUID.randomUUID().toString(), userId, hash(rawToken), now, now.plus(TTL), null);
    }

    public static PasswordResetToken reconstitute(String id, String userId, String tokenHash, Instant createdAt,
                                                    Instant expiresAt, Instant usedAt) {
        return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, usedAt);
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is guaranteed to exist on every JVM", e);
        }
    }

    public boolean matches(String rawToken) {
        return tokenHash.equals(hash(rawToken));
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isValid(String rawToken, Instant now) {
        return !isUsed() && !isExpired(now) && matches(rawToken);
    }

    public PasswordResetToken markUsed() {
        return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, Instant.now());
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
}
