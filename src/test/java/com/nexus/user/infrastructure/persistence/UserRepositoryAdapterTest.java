package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterTest {

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
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void saveThenFindByEmail_roundTripsTheUser() {
        User user = User.register("bob@example.com", "hashed-pw", "Bob Tran",
                new RoleId(UUID.randomUUID().toString()));

        userRepositoryAdapter.save(user);
        Optional<User> found = userRepositoryAdapter.findByEmail("bob@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Bob Tran");
    }

    @Test
    void findByEmail_returnsEmptyWhenNotFound() {
        assertThat(userRepositoryAdapter.findByEmail("nobody@example.com")).isEmpty();
    }
}
