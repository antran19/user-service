package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.infrastructure.persistence.OutboxJpaRepository;
import com.nexus.user.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the Outbox pattern's transactional guarantee for real: {@link RegisterUserUseCase#register}
 * runs against a real Postgres (via Testcontainers) with real adapters wired end to end, with NO
 * class-level {@code @Transactional} — that annotation on a test class would open its own transaction
 * around each test and roll it back at the end regardless of what the use case itself does, which would
 * hide whether the use case's own {@code @Transactional} boundary is doing anything at all.
 *
 * <p>{@link RegisterUserUseCaseTest} covers the same use case with mocked ports (fast, no DB); this
 * class instead answers "if the outbox write fails mid-transaction, does the user save really roll
 * back?" — something a mock-based test structurally cannot prove.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RegisterUserUseCaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OutboxJpaRepository outboxJpaRepository;

    @Test
    void register_persistsBothTheUserAndAnUnpublishedOutboxRow() {
        UserRegistrationResult result = registerUserUseCase.register(
                new RegisterUserCommand("dave.pham@example.com", "longenough", "Dave Pham"));

        assertThat(userJpaRepository.findByEmail("dave.pham@example.com")).isPresent();

        assertThat(outboxJpaRepository.findAll())
                .anySatisfy(row -> {
                    assertThat(row.getAggregateId()).isEqualTo(result.userId());
                    assertThat(row.getEventType()).isEqualTo("UserRegistered");
                    assertThat(row.getPublishedAt()).isNull();
                    assertThat(row.getPayload()).contains("dave.pham@example.com");
                });
    }

    /**
     * Separate nested Spring context (via {@code @Import}) that substitutes a throwing
     * {@link EventPublisherPort} for the real outbox-writing adapter, so the user-save-then-throw
     * sequence happens inside the use case's own real {@code @Transactional} boundary against the
     * real database, letting us assert on rollback rather than infer it.
     */
    @Nested
    @Import(WhenOutboxWriteFails.FailingEventPublisherConfig.class)
    class WhenOutboxWriteFails {

        @Autowired
        private RegisterUserUseCase registerUserUseCase;

        @Autowired
        private UserJpaRepository userJpaRepository;

        @Test
        void register_rollsBackTheUserSaveWhenTheOutboxWriteThrows() {
            assertThatThrownBy(() -> registerUserUseCase.register(
                    new RegisterUserCommand("erin.vo@example.com", "longenough", "Erin Vo")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("simulated outbox failure");

            assertThat(userJpaRepository.findByEmail("erin.vo@example.com"))
                    .as("user save must roll back when the outbox write in the same transaction fails")
                    .isEmpty();
        }

        @TestConfiguration
        static class FailingEventPublisherConfig {
            @Bean
            @Primary
            EventPublisherPort failingEventPublisherPort() {
                return event -> {
                    throw new RuntimeException("simulated outbox failure");
                };
            }
        }
    }
}
