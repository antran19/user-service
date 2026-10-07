package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.SellerRequest;
import com.nexus.user.domain.model.SellerRequestStatus;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SellerRequestRepositoryAdapter.class, UserRepositoryAdapter.class})
class SellerRequestRepositoryAdapterTest {

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
    private SellerRequestRepositoryAdapter sellerRequestRepositoryAdapter;

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private TestEntityManager entityManager;

    private String realUserId() {
        // seller_requests.user_id has a real FK to users(id), so every test needs a real row
        // to attach to -- not just any UUID, the way UserRepositoryAdapterTest can get away with.
        User user = User.register(UUID.randomUUID() + "@example.com", "hash", "Test User",
                new RoleId(UUID.randomUUID().toString()));
        userRepositoryAdapter.save(user);
        return user.getId();
    }

    @Test
    void saveThenFindById_roundTripsTheRequest() {
        String userId = realUserId();
        SellerRequest request = SellerRequest.create(userId);

        sellerRequestRepositoryAdapter.save(request);
        Optional<SellerRequest> found = sellerRequestRepositoryAdapter.findById(request.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getStatus()).isEqualTo(SellerRequestStatus.PENDING);
    }

    @Test
    void findPendingByUserId_findsOnlyThePendingOne() {
        String userId = realUserId();
        SellerRequest rejected = SellerRequest.create(userId).reject(UUID.randomUUID().toString());
        sellerRequestRepositoryAdapter.save(rejected);

        assertThat(sellerRequestRepositoryAdapter.findPendingByUserId(userId)).isEmpty();

        SellerRequest pending = SellerRequest.create(userId);
        sellerRequestRepositoryAdapter.save(pending);

        Optional<SellerRequest> found = sellerRequestRepositoryAdapter.findPendingByUserId(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(pending.getId());
    }

    @Test
    void save_rejectsASecondPendingRequestForTheSameUser() {
        // DB-level partial unique index (idx_seller_requests_one_pending_per_user) is the real
        // guard against a race between two concurrent "request to become a seller" calls; the
        // application-level check in RequestSellerUpgradeUseCase alone cannot close that race.
        //
        // Going through TestEntityManager.flush() (rather than a real request, which commits via
        // Spring's repository proxy) surfaces the raw Hibernate exception instead of Spring's
        // translated DataIntegrityViolationException -- still proof the DB constraint fires.
        String userId = realUserId();
        sellerRequestRepositoryAdapter.save(SellerRequest.create(userId));
        entityManager.flush(); // force the first INSERT to hit the DB before the second one races it

        assertThatThrownBy(() -> {
            sellerRequestRepositoryAdapter.save(SellerRequest.create(userId));
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void findByStatus_returnsOnlyMatchingRequestsOrderedByRequestedAt() {
        String userA = realUserId();
        String userB = realUserId();
        sellerRequestRepositoryAdapter.save(SellerRequest.create(userA));
        sellerRequestRepositoryAdapter.save(SellerRequest.create(userB).approve(UUID.randomUUID().toString()));

        assertThat(sellerRequestRepositoryAdapter.findByStatus(SellerRequestStatus.PENDING))
                .extracting(SellerRequest::getUserId)
                .containsExactly(userA);
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        assertThat(sellerRequestRepositoryAdapter.findById(UUID.randomUUID().toString())).isEmpty();
    }
}
