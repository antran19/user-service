package com.nexus.user.infrastructure.persistence;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// V19: the database itself refuses a seller request status the application does not know.
// No surrounding transaction: each insert must commit (or fail) on its own.
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SellerRequestCheckConstraintTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private JdbcTemplate jdbcTemplate;

    private void insertRequest(String status) {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO users (id, email, hashed_password, full_name, role_id) "
                        + "VALUES (?, ?, 'x', 'Test', (SELECT id FROM roles LIMIT 1))",
                userId, userId + "@example.com");
        jdbcTemplate.update("INSERT INTO seller_requests (user_id, status) VALUES (?, ?)", userId, status);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "APPROVED", "REJECTED"})
    void everyStatusTheApplicationKnows_isAccepted(String status) {
        assertThatCode(() -> insertRequest(status)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC", "pending", "CANCELLED", ""})
    void aStatusOutsideTheEnum_isRejected(String status) {
        assertThatThrownBy(() -> insertRequest(status))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_seller_requests_status");
    }
}
