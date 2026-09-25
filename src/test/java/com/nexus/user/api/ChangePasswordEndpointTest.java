package com.nexus.user.api;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import com.nexus.user.infrastructure.persistence.RoleRepositoryAdapter;
import com.nexus.user.infrastructure.persistence.UserRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class ChangePasswordEndpointTest {

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

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepositoryAdapter userRepositoryAdapter;
    @Autowired private RoleRepositoryAdapter roleRepositoryAdapter;
    @Autowired private PasswordHasherPort passwordHasherPort;

    private String userId;

    @BeforeEach
    void createUser() {
        Role buyerRole = roleRepositoryAdapter.findByCode("BUYER").orElseThrow();
        User user = User.register("carol@example.com", passwordHasherPort.hash("oldpassword"), "Carol Le",
                new RoleId(buyerRole.id()));
        userId = userRepositoryAdapter.save(user).getId();
    }

    @Test
    void changePassword_succeedsWithValidTokenAndCorrectOldPassword() throws Exception {
        String token = jwtTokenProvider.generateToken(userId, "BUYER", List.of("PROFILE.CHANGE_PASSWORD"));

        mockMvc.perform(put("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"oldpassword","newPassword":"newlongpassword"}"""))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_returns401WithoutToken() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"oldpassword","newPassword":"newlongpassword"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    @Test
    void changePassword_returns403WhenTokenLacksPrivilege() throws Exception {
        String token = jwtTokenProvider.generateToken(userId, "BUYER", List.of("PROFILE.VIEW"));

        mockMvc.perform(put("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"oldpassword","newPassword":"newlongpassword"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_returns401WhenOldPasswordWrong() throws Exception {
        String token = jwtTokenProvider.generateToken(userId, "BUYER", List.of("PROFILE.CHANGE_PASSWORD"));

        mockMvc.perform(put("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"wrongpassword","newPassword":"newlongpassword"}"""))
                .andExpect(status().isUnauthorized());
    }
}
