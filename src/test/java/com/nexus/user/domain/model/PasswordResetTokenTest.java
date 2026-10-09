package com.nexus.user.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetTokenTest {

    @Test
    void issue_generatesIdAndHashesTheRawToken() {
        PasswordResetToken resetToken = PasswordResetToken.issue("user-1", "raw-token-value");

        assertThat(resetToken.getId()).isNotBlank();
        assertThat(resetToken.getUserId()).isEqualTo("user-1");
        assertThat(resetToken.getTokenHash()).isNotEqualTo("raw-token-value");
        assertThat(resetToken.getExpiresAt()).isAfter(resetToken.getCreatedAt());
        assertThat(resetToken.isUsed()).isFalse();
    }

    @Test
    void matches_returnsTrueOnlyForTheOriginalRawToken() {
        PasswordResetToken resetToken = PasswordResetToken.issue("user-1", "raw-token-value");

        assertThat(resetToken.matches("raw-token-value")).isTrue();
        assertThat(resetToken.matches("wrong-token")).isFalse();
    }

    @Test
    void isValid_isFalseOnceExpired() {
        PasswordResetToken resetToken = PasswordResetToken.reconstitute("id-1", "user-1",
                PasswordResetToken.hash("raw-token-value"), Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.MINUTES), null);

        assertThat(resetToken.isValid("raw-token-value", Instant.now())).isFalse();
    }

    @Test
    void isValid_isFalseOnceUsed() {
        PasswordResetToken resetToken = PasswordResetToken.issue("user-1", "raw-token-value").markUsed();

        assertThat(resetToken.isValid("raw-token-value", Instant.now())).isFalse();
        assertThat(resetToken.isUsed()).isTrue();
    }

    @Test
    void isValid_isTrueForAFreshUnusedMatchingToken() {
        PasswordResetToken resetToken = PasswordResetToken.issue("user-1", "raw-token-value");

        assertThat(resetToken.isValid("raw-token-value", Instant.now())).isTrue();
    }

    @Test
    void hash_isDeterministicSoItCanBeUsedAsALookupKey() {
        assertThat(PasswordResetToken.hash("raw-token-value")).isEqualTo(PasswordResetToken.hash("raw-token-value"));
    }
}
