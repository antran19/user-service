package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.ReputationPenalty;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ReputationPenaltyRepositoryAdapter.class, UserRepositoryAdapter.class})
class ReputationPenaltyRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private ReputationPenaltyRepositoryAdapter adapter;
    @Autowired private UserRepositoryAdapter userRepositoryAdapter;
    @Autowired private TestEntityManager entityManager;

    private String realUserId() {
        User user = User.register(UUID.randomUUID() + "@example.com", "hash", "Test User",
                new RoleId(UUID.randomUUID().toString()));
        userRepositoryAdapter.save(user);
        return user.getId();
    }

    @Test
    void save_thenFindByUserId_roundTripsFields() {
        String userId = realUserId();
        adapter.save(ReputationPenalty.create(userId, "AUCTION_PAYMENT_TIMEOUT", 10, "auction-1"));
        entityManager.flush();
        entityManager.clear();

        List<ReputationPenalty> penalties = adapter.findByUserId(userId);

        assertThat(penalties).hasSize(1);
        assertThat(penalties.get(0).getPoints()).isEqualTo(10);
        assertThat(penalties.get(0).getReferenceId()).isEqualTo("auction-1");
    }

    @Test
    void existsByReferenceIdAndReason_trueAfterApplied() {
        String userId = realUserId();
        adapter.save(ReputationPenalty.create(userId, "AUCTION_PAYMENT_TIMEOUT", 10, "auction-2"));
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.existsByReferenceIdAndReason("auction-2", "AUCTION_PAYMENT_TIMEOUT")).isTrue();
        assertThat(adapter.existsByReferenceIdAndReason("auction-3", "AUCTION_PAYMENT_TIMEOUT")).isFalse();
    }
}
