package com.nexus.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RatingTest {

    @Test
    void create_generatesIdAndStoresFields() {
        Rating rating = Rating.create(TransactionType.ORDER, "order-1", "rater-1", "rated-1", 5, "Great!");

        assertThat(rating.getId()).isNotBlank();
        assertThat(rating.getTransactionType()).isEqualTo(TransactionType.ORDER);
        assertThat(rating.getTransactionId()).isEqualTo("order-1");
        assertThat(rating.getRaterId()).isEqualTo("rater-1");
        assertThat(rating.getRatedUserId()).isEqualTo("rated-1");
        assertThat(rating.getScore()).isEqualTo(5);
        assertThat(rating.getComment()).isEqualTo("Great!");
        assertThat(rating.getCreatedAt()).isNotNull();
    }
}
