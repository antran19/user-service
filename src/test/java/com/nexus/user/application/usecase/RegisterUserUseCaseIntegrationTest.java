package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ValidationException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GetMyProfileUseCase getMyProfileUseCase;

    @Autowired
    private UpdateMyProfileUseCase updateMyProfileUseCase;

    @Test
    void register_persistsBothTheUserAndAnUnpublishedOutboxRow() {
        UserRegistrationResult result = registerUserUseCase.register(
                new RegisterUserCommand("dave.pham@example.com", "longenough", "Dave Pham", null, null, true, true));

        assertThat(userJpaRepository.findByEmail("dave.pham@example.com")).isPresent();

        assertThat(outboxJpaRepository.findAll())
                .anySatisfy(row -> {
                    assertThat(row.getAggregateId()).isEqualTo(result.userId());
                    assertThat(row.getEventType()).isEqualTo("UserRegistered");
                    assertThat(row.getPublishedAt()).isNull();
                    assertThat(row.getPayload()).contains("dave.pham@example.com");
                });
    }

    @Test
    void register_storesThePhoneBirthDateAndConsentsInTheDatabase() {
        registerUserUseCase.register(new RegisterUserCommand("frank.le@example.com", "longenough", "Frank Le",
                "+84 901 234 567", LocalDate.of(1990, 3, 20), true, true));

        var saved = userJpaRepository.findByEmail("frank.le@example.com").orElseThrow();
        assertThat(saved.getPhone()).isEqualTo("+84901234567");
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1990, 3, 20));
        assertThat(saved.getTermsAcceptedAt()).isNotNull();
        assertThat(saved.getEscrowAcknowledgedAt()).isNotNull();
    }

    @Test
    void register_withoutConsent_storesNothing() {
        assertThatThrownBy(() -> registerUserUseCase.register(new RegisterUserCommand("gina.ho@example.com",
                "longenough", "Gina Ho", null, null, false, false)))
                .isInstanceOf(ValidationException.class);

        assertThat(userJpaRepository.findByEmail("gina.ho@example.com")).isEmpty();
    }

    @Test
    void myProfile_isReadBackAfterSignUp_andUpdatingItChangesOnlyTheNameAndPhone() {
        UserRegistrationResult registered = registerUserUseCase.register(new RegisterUserCommand("hana.vu@example.com",
                "longenough", "Hana Vu", "0901234567", LocalDate.of(1992, 8, 9), true, true));

        ProfileResult before = getMyProfileUseCase.get(registered.userId());
        assertThat(before.roleCode()).isEqualTo("BUYER");
        assertThat(before.phone()).isEqualTo("0901234567");
        assertThat(before.birthDate()).isEqualTo(LocalDate.of(1992, 8, 9));

        ProfileResult after = updateMyProfileUseCase.update(registered.userId(), "Hana T. Vu", "+84 988 777 666");

        assertThat(after.fullName()).isEqualTo("Hana T. Vu");
        assertThat(after.phone()).isEqualTo("+84988777666");
        var stored = userJpaRepository.findByEmail("hana.vu@example.com").orElseThrow();
        assertThat(stored.getPhone()).isEqualTo("+84988777666");
        assertThat(stored.getBirthDate()).isEqualTo(LocalDate.of(1992, 8, 9));
        assertThat(stored.getTermsAcceptedAt()).isEqualTo(before.termsAcceptedAt());
        assertThat(stored.getEmail()).isEqualTo("hana.vu@example.com");
    }

    @Test
    void myProfile_aBlankPhoneClearsItInTheDatabase() {
        UserRegistrationResult registered = registerUserUseCase.register(new RegisterUserCommand("ivy.do@example.com",
                "longenough", "Ivy Do", "0901234567", null, true, true));

        updateMyProfileUseCase.update(registered.userId(), "Ivy Do", "");

        assertThat(userJpaRepository.findByEmail("ivy.do@example.com").orElseThrow().getPhone()).isNull();
    }

    @Test
    void theDatabaseItselfRefusesAPhoneInTheWrongFormat() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO users (id, email, hashed_password, full_name, role_id, phone) "
                        + "VALUES (gen_random_uuid(), 'bad.phone@example.com', 'x', 'Bad Phone', (SELECT id FROM roles LIMIT 1), '12345')"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_users_phone_format");
    }

    @Test
    void theDatabaseAcceptsAnAccountWithoutAnyOfTheNewDetails() {
        jdbcTemplate.update("INSERT INTO users (id, email, hashed_password, full_name, role_id) "
                + "VALUES (gen_random_uuid(), 'old.account@example.com', 'x', 'Old Account', (SELECT id FROM roles LIMIT 1))");

        assertThat(userJpaRepository.findByEmail("old.account@example.com")).isPresent();
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
                    new RegisterUserCommand("erin.vo@example.com", "longenough", "Erin Vo", null, null, true, true)))
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
