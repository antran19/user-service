package com.nexus.user.infrastructure.persistence;

import com.nexus.user.domain.model.Role;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RoleRepositoryAdapter.class)
class RoleRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db").withUsername("nexus").withPassword("nexus");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private RoleRepositoryAdapter roleRepositoryAdapter;

    @Test
    void findByCode_returnsSeedRoleWithItsPrivileges() {
        Optional<Role> role = roleRepositoryAdapter.findByCode("BUYER");

        assertThat(role).isPresent();
        assertThat(role.get().privilegeCodes()).contains("PROFILE.CHANGE_PASSWORD", "AUTH.LOGIN");
    }

    @Test
    void admin_holdsCatalogPrivilegesIncludingManageAny() {
        Role admin = roleRepositoryAdapter.findByCode("ADMIN").orElseThrow();

        assertThat(admin.privilegeCodes()).contains(
                "CATEGORY.CREATE", "CATEGORY.UPDATE", "CATEGORY.DELETE",
                "PRODUCT.CREATE", "PRODUCT.UPDATE", "PRODUCT.DELETE",
                "PRODUCT.MANAGE_ANY");
    }

    @Test
    void seller_holdsOwnProductPrivilegesOnly_noCategoryPrivilegesAndNoManageAny() {
        Role seller = roleRepositoryAdapter.findByCode("SELLER").orElseThrow();

        assertThat(seller.privilegeCodes()).contains("PRODUCT.CREATE", "PRODUCT.UPDATE", "PRODUCT.DELETE");
        // Categories are shared global taxonomy (ADMIN-only per the catalog-service spec), and
        // MANAGE_ANY would let a seller modify other sellers' products.
        assertThat(seller.privilegeCodes()).doesNotContain(
                "CATEGORY.CREATE", "CATEGORY.UPDATE", "CATEGORY.DELETE", "PRODUCT.MANAGE_ANY");
    }

    @Test
    void findByCode_returnsEmptyForUnknownCode() {
        assertThat(roleRepositoryAdapter.findByCode("NOT_A_ROLE")).isEmpty();
    }
}
