package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.Rating;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.TransactionType;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RatingRepositoryAdapter.class, UserRepositoryAdapter.class})
class RatingRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private RatingRepositoryAdapter adapter;
    @Autowired private UserRepositoryAdapter userRepositoryAdapter;
    @Autowired private TestEntityManager entityManager;

    private String realUserId() {
        User user = User.register(UUID.randomUUID() + "@example.com", "hash", "Test User",
                new RoleId(UUID.randomUUID().toString()));
        userRepositoryAdapter.save(user);
        return user.getId();
    }

    @Test
    void save_persistsRating() {
        String rater = realUserId();
        String rated = realUserId();
        Rating rating = Rating.create(TransactionType.ORDER, "order-1", rater, rated, 5, "Great!");

        adapter.save(rating);
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.existsByTransactionIdAndRaterId("order-1", rater)).isTrue();
    }

    @Test
    void existsByTransactionIdAndRaterId_falseWhenNoSuchRating() {
        assertThat(adapter.existsByTransactionIdAndRaterId("order-404", UUID.randomUUID().toString())).isFalse();
    }

    @Test
    void save_rejectsDuplicateRatingForSameTransactionAndRater() {
        String rater = realUserId();
        String rated = realUserId();
        adapter.save(Rating.create(TransactionType.ORDER, "order-1", rater, rated, 5, null));
        entityManager.flush();

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            adapter.save(Rating.create(TransactionType.ORDER, "order-1", rater, rated, 2, null));
            entityManager.flush();
        });
    }
}
