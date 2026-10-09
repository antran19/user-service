package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.PasswordResetToken;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PasswordResetTokenRepositoryAdapter.class, UserRepositoryAdapter.class})
class PasswordResetTokenRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private PasswordResetTokenRepositoryAdapter adapter;
    @Autowired private UserRepositoryAdapter userRepositoryAdapter;
    @Autowired private TestEntityManager entityManager;

    private String realUserId() {
        User user = User.register(UUID.randomUUID() + "@example.com", "hash", "Test User",
                new RoleId(UUID.randomUUID().toString()));
        userRepositoryAdapter.save(user);
        return user.getId();
    }

    @Test
    void save_thenFindByTokenHash_roundTripsFields() {
        String userId = realUserId();
        PasswordResetToken resetToken = PasswordResetToken.issue(userId, "raw-token");
        adapter.save(resetToken);
        entityManager.flush();
        entityManager.clear();

        PasswordResetToken found = adapter.findByTokenHash(PasswordResetToken.hash("raw-token")).orElseThrow();

        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.isUsed()).isFalse();
    }

    @Test
    void findByTokenHash_reflectsMarkUsed() {
        String userId = realUserId();
        PasswordResetToken resetToken = PasswordResetToken.issue(userId, "raw-token-2");
        adapter.save(resetToken);
        adapter.save(resetToken.markUsed());
        entityManager.flush();
        entityManager.clear();

        PasswordResetToken found = adapter.findByTokenHash(PasswordResetToken.hash("raw-token-2")).orElseThrow();

        assertThat(found.isUsed()).isTrue();
    }

    @Test
    void findByTokenHash_isEmptyForAnUnknownToken() {
        assertThat(adapter.findByTokenHash(PasswordResetToken.hash("never-issued"))).isEmpty();
    }
}
