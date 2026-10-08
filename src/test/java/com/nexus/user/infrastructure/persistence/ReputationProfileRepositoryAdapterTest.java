package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.ReputationProfile;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ReputationProfileRepositoryAdapter.class, UserRepositoryAdapter.class})
class ReputationProfileRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private ReputationProfileRepositoryAdapter adapter;
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
        ReputationProfile profile = ReputationProfile.createDefault(userId).applyRating(5);
        adapter.save(profile);
        entityManager.flush();
        entityManager.clear();

        Optional<ReputationProfile> reloaded = adapter.findByUserId(userId);

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getScore()).isEqualTo(54);
        assertThat(reloaded.get().getTotalRatings()).isEqualTo(1);
    }

    @Test
    void findByUserId_returnsEmptyWhenNoProfile() {
        assertThat(adapter.findByUserId(UUID.randomUUID().toString())).isEmpty();
    }
}
