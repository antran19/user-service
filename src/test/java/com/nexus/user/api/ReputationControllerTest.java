package com.nexus.user.api;

import com.nexus.user.api.mapper.UserApiMapperImpl;
import com.nexus.user.application.usecase.GetReputationUseCase;
import com.nexus.user.application.usecase.ListReputationPenaltiesUseCase;
import com.nexus.user.application.usecase.ReputationResult;
import com.nexus.user.infrastructure.config.SecurityConfig;
import com.nexus.common.security.JwtAuthenticationFilter;
import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.security.PrivilegeAuthorizationAspect;
import com.nexus.common.web.GlobalExceptionHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
