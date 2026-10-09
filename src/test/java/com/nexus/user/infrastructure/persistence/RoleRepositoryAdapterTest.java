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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    @Test
    void save_createsNewRoleWithGivenPrivileges() {
        Role role = new Role(UUID.randomUUID().toString(), "VIEWER_TEST", "Viewer (test)",
                Set.of("PROFILE.VIEW"));

        roleRepositoryAdapter.save(role);

        Role reloaded = roleRepositoryAdapter.findByCode("VIEWER_TEST").orElseThrow();
        assertThat(reloaded.name()).isEqualTo("Viewer (test)");
        assertThat(reloaded.privilegeCodes()).containsExactly("PROFILE.VIEW");
    }

    @Test
    void save_updatesPrivilegesOfExistingRole() {
        Role role = new Role(UUID.randomUUID().toString(), "EDITABLE_TEST", "Editable (test)",
                Set.of("PROFILE.VIEW"));
        roleRepositoryAdapter.save(role);

        Role updated = new Role(role.id(), role.code(), "Renamed", Set.of("PROFILE.VIEW", "PROFILE.UPDATE"));
        roleRepositoryAdapter.save(updated);

        Role reloaded = roleRepositoryAdapter.findById(role.id()).orElseThrow();
        assertThat(reloaded.name()).isEqualTo("Renamed");
        assertThat(reloaded.privilegeCodes()).containsExactlyInAnyOrder("PROFILE.VIEW", "PROFILE.UPDATE");
    }

    @Test
    void findAll_includesSeededRoles() {
        List<Role> roles = roleRepositoryAdapter.findAll();

        assertThat(roles).extracting(Role::code).contains("ADMIN", "SELLER", "BUYER", "SUPPORT_STAFF");
    }

    @Test
    void deleteById_removesTheRole() {
        Role role = new Role(UUID.randomUUID().toString(), "DELETE_ME_TEST", "Delete me", Set.of());
        roleRepositoryAdapter.save(role);

        roleRepositoryAdapter.deleteById(role.id());

        assertThat(roleRepositoryAdapter.findById(role.id())).isEmpty();
    }
}
