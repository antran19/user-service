package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.RatableTransaction;
import com.nexus.user.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RatableTransactionRepositoryAdapter.class)
class RatableTransactionRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private RatableTransactionRepositoryAdapter adapter;

    private RatableTransaction tx(String id, TransactionType type, String buyer, String seller) {
        return new RatableTransaction(id, type, buyer, seller, Instant.parse("2026-10-23T10:00:00Z"));
    }

    @Test
    void insertThenFind_returnsBothPartiesAndTheDate() {
        adapter.insertIfAbsent(tx("order-1", TransactionType.ORDER, "buyer-1", "seller-1"));

        RatableTransaction found = adapter.find("order-1", TransactionType.ORDER).orElseThrow();

        assertThat(found.buyerId()).isEqualTo("buyer-1");
        assertThat(found.sellerId()).isEqualTo("seller-1");
        assertThat(found.completedAt()).isEqualTo(Instant.parse("2026-10-23T10:00:00Z"));
    }

    // Kafka delivers at least once: the same event twice must neither fail nor change the row.
    @Test
    void insertingTheSameTransactionTwice_keepsTheFirstAndDoesNotFail() {
        adapter.insertIfAbsent(tx("order-2", TransactionType.ORDER, "buyer-1", "seller-1"));
        adapter.insertIfAbsent(tx("order-2", TransactionType.ORDER, "buyer-X", "seller-X"));

        assertThat(adapter.find("order-2", TransactionType.ORDER).orElseThrow().buyerId()).isEqualTo("buyer-1");
    }

    @Test
    void theSameIdAsAnOrderAndAsAnAuction_areTwoDifferentTransactions() {
        adapter.insertIfAbsent(tx("x-1", TransactionType.ORDER, "buyer-1", "seller-1"));

        assertThat(adapter.find("x-1", TransactionType.ORDER)).isPresent();
        assertThat(adapter.find("x-1", TransactionType.AUCTION)).isEmpty();
    }

    @Test
    void findOfAnUnknownId_isEmpty() {
        assertThat(adapter.find("never-heard-of-it", TransactionType.ORDER)).isEmpty();
    }
}
