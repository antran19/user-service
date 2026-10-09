package com.nexus.user.infrastructure.persistence;

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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BlacklistedTokenRepositoryAdapter.class)
class BlacklistedTokenRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private BlacklistedTokenRepositoryAdapter adapter;
    @Autowired private TestEntityManager entityManager;

    @Test
    void blacklist_thenIsBlacklisted_isTrue() {
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        adapter.blacklist("jti-1", expiresAt);
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.isBlacklisted("jti-1")).isTrue();
    }

    @Test
    void isBlacklisted_isFalseForAnUnknownJti() {
        assertThat(adapter.isBlacklisted("never-blacklisted")).isFalse();
    }

    @Test
    void isBlacklisted_isFalseForNullJti() {
        assertThat(adapter.isBlacklisted(null)).isFalse();
    }
}
