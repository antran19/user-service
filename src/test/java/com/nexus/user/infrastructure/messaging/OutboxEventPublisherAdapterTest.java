package com.nexus.user.infrastructure.messaging;

import com.nexus.common.events.UserRegisteredEvent;
import com.nexus.user.infrastructure.persistence.OutboxJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OutboxEventPublisherAdapter.class, JacksonAutoConfiguration.class})
class OutboxEventPublisherAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OutboxEventPublisherAdapter adapter;

    @Autowired
    private OutboxJpaRepository outboxJpaRepository;

    @Test
    void publish_writesUnpublishedOutboxRow() {
        adapter.publish(new UserRegisteredEvent("user-1", "alice@example.com", "Alice Nguyen"));

        var rows = outboxJpaRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getEventType()).isEqualTo("UserRegistered");
        assertThat(rows.get(0).getAggregateId()).isEqualTo("user-1");
        assertThat(rows.get(0).getPublishedAt()).isNull();
        assertThat(rows.get(0).getPayload()).contains("alice@example.com");
    }
}
