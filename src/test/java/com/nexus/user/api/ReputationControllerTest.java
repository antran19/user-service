package com.nexus.user.api;

import com.nexus.user.api.mapper.UserApiMapperImpl;
import com.nexus.user.application.usecase.AdjustmentResult;
import com.nexus.user.application.usecase.AdminAdjustReputationUseCase;
import com.nexus.user.application.usecase.GetReputationUseCase;
import com.nexus.user.application.usecase.ListReputationAdjustmentsUseCase;
import com.nexus.user.application.usecase.ListReputationPenaltiesUseCase;
import com.nexus.user.application.usecase.ReputationResult;
import com.nexus.user.infrastructure.config.SecurityConfig;
import com.nexus.common.security.JwtAuthenticationFilter;
import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.security.PrivilegeAuthorizationAspect;
import com.nexus.common.web.GlobalExceptionHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReputationController.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, UserApiMapperImpl.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, PrivilegeAuthorizationAspect.class})
class ReputationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private GetReputationUseCase getReputationUseCase;
    @MockBean private ListReputationPenaltiesUseCase listReputationPenaltiesUseCase;
    @MockBean private AdminAdjustReputationUseCase adminAdjustReputationUseCase;
    @MockBean private ListReputationAdjustmentsUseCase listReputationAdjustmentsUseCase;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    private void authenticateAs(String subject, String... privileges) {
        when(jwtTokenProvider.isValid("good-token")).thenReturn(true);
        Claims claims = Jwts.claims().subject(subject).add("privileges", List.of(privileges)).build();
        when(jwtTokenProvider.parseClaims("good-token")).thenReturn(claims);
    }

    @Test
    void getReputation_returns403WithoutPrivilege() throws Exception {
        authenticateAs("buyer-1");

        mockMvc.perform(get("/api/v1/users/user-1/reputation").header("Authorization", "Bearer good-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReputation_returns200() throws Exception {
        authenticateAs("buyer-1", "REPUTATION.VIEW");
        when(getReputationUseCase.getReputation("user-1"))
                .thenReturn(new ReputationResult("user-1", 54, "TRUSTED", 1, 5.0));

        mockMvc.perform(get("/api/v1/users/user-1/reputation").header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(54))
                .andExpect(jsonPath("$.data.trustLevel").value("TRUSTED"));
    }

    @Test
    void listPenalties_returns403WithoutPrivilege() throws Exception {
        authenticateAs("buyer-1", "REPUTATION.VIEW");

        mockMvc.perform(get("/api/v1/users/user-1/reputation/penalties").header("Authorization", "Bearer good-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPenalties_returns200ForAdmin() throws Exception {
        authenticateAs("admin-1", "REPUTATION.PENALTY.VIEW");
        when(listReputationPenaltiesUseCase.list("user-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users/user-1/reputation/penalties").header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk());
    }

    @Test
    void adjust_returns403WithoutPrivilege() throws Exception {
        authenticateAs("admin-1", "REPUTATION.VIEW");

        mockMvc.perform(post("/api/v1/users/user-1/reputation/adjust")
                        .header("Authorization", "Bearer good-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"delta":-5,"reason":"Manual correction"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void adjust_returns200ForAdmin() throws Exception {
        authenticateAs("admin-1", "USER.REPUTATION.ADJUST");
        when(adminAdjustReputationUseCase.adjust("user-1", "admin-1", -5, "Manual correction"))
                .thenReturn(new ReputationResult("user-1", 45, "NORMAL", 0, 0.0));

        mockMvc.perform(post("/api/v1/users/user-1/reputation/adjust")
                        .header("Authorization", "Bearer good-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"delta":-5,"reason":"Manual correction"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(45));
    }

    @Test
    void listAdjustments_returns200ForAdmin() throws Exception {
        authenticateAs("admin-1", "USER.REPUTATION.ADJUST");
        when(listReputationAdjustmentsUseCase.list("user-1")).thenReturn(
                List.of(new AdjustmentResult("adj-1", "user-1", "admin-1", -5, "Manual correction", Instant.now())));

        mockMvc.perform(get("/api/v1/users/user-1/reputation/adjustments").header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].delta").value(-5));
    }
}
