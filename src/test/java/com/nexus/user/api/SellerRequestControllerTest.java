package com.nexus.user.api;

import com.nexus.user.api.mapper.UserApiMapperImpl;
import com.nexus.user.application.usecase.ListSellerRequestsUseCase;
import com.nexus.user.application.usecase.RequestSellerUpgradeUseCase;
import com.nexus.user.application.usecase.ReviewSellerRequestUseCase;
import com.nexus.user.application.usecase.SellerRequestResult;
import com.nexus.user.infrastructure.config.SecurityConfig;
import com.nexus.common.security.JwtAuthenticationFilter;
import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.security.PrivilegeAuthorizationAspect;
import com.nexus.common.web.GlobalExceptionHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerRequestController.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, UserApiMapperImpl.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, PrivilegeAuthorizationAspect.class})
class SellerRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private RequestSellerUpgradeUseCase requestSellerUpgradeUseCase;
    @MockBean private ListSellerRequestsUseCase listSellerRequestsUseCase;
    @MockBean private ReviewSellerRequestUseCase reviewSellerRequestUseCase;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    private void authenticateAs(String subject, String... privileges) {
        when(jwtTokenProvider.isValid("good-token")).thenReturn(true);
        Claims claims = Jwts.claims().subject(subject).add("privileges", List.of(privileges)).build();
        when(jwtTokenProvider.parseClaims("good-token")).thenReturn(claims);
    }

    @Test
    void requestUpgrade_returns401WithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/seller-requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestUpgrade_returns201ForAnyAuthenticatedUser() throws Exception {
        // Any authenticated user (BUYER by default) may ask to become a seller -- this is
        // deliberately NOT @RequiresPrivilege-gated, unlike the admin review endpoints below.
        authenticateAs("buyer-id");
        when(requestSellerUpgradeUseCase.requestUpgrade("buyer-id")).thenReturn(
                new SellerRequestResult("req-1", "buyer-id", "PENDING", Instant.now(), null, null));

        mockMvc.perform(post("/api/v1/users/me/seller-requests")
                        .header("Authorization", "Bearer good-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void listRequests_returns403WithoutPrivilege() throws Exception {
        authenticateAs("buyer-id");

        mockMvc.perform(get("/api/v1/users/seller-requests")
                        .header("Authorization", "Bearer good-token")
                        .param("status", "PENDING"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listRequests_returns200WithPrivilege() throws Exception {
        authenticateAs("admin-id", "USER.REVIEW_SELLER_REQUESTS");
        when(listSellerRequestsUseCase.list(com.nexus.user.domain.model.SellerRequestStatus.PENDING))
                .thenReturn(List.of(new SellerRequestResult("req-1", "buyer-id", "PENDING", Instant.now(), null, null)));

        mockMvc.perform(get("/api/v1/users/seller-requests")
                        .header("Authorization", "Bearer good-token")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value("buyer-id"));
    }

    @Test
    void approve_returns403WithoutPrivilege() throws Exception {
        authenticateAs("buyer-id");

        mockMvc.perform(post("/api/v1/users/seller-requests/req-1/approve")
                        .header("Authorization", "Bearer good-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approve_returns200AndPassesReviewerIdentityToUseCase() throws Exception {
        authenticateAs("admin-id", "USER.REVIEW_SELLER_REQUESTS");
        when(reviewSellerRequestUseCase.review("req-1", "admin-id", true)).thenReturn(
                new SellerRequestResult("req-1", "buyer-id", "APPROVED", Instant.now(), Instant.now(), "admin-id"));

        mockMvc.perform(post("/api/v1/users/seller-requests/req-1/approve")
                        .header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(reviewSellerRequestUseCase).review(eq("req-1"), eq("admin-id"), eq(true));
    }

    @Test
    void reject_returns200AndPassesApproveFalse() throws Exception {
        authenticateAs("admin-id", "USER.REVIEW_SELLER_REQUESTS");
        when(reviewSellerRequestUseCase.review("req-1", "admin-id", false)).thenReturn(
                new SellerRequestResult("req-1", "buyer-id", "REJECTED", Instant.now(), Instant.now(), "admin-id"));

        mockMvc.perform(post("/api/v1/users/seller-requests/req-1/reject")
                        .header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        verify(reviewSellerRequestUseCase).review(eq("req-1"), eq("admin-id"), eq(false));
    }
}
