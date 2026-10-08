package com.nexus.user.api;

import com.nexus.user.api.mapper.UserApiMapperImpl;
import com.nexus.user.application.usecase.RateTransactionUseCase;
import com.nexus.user.application.usecase.RatingResult;
import com.nexus.user.infrastructure.config.SecurityConfig;
import com.nexus.common.core.exception.ConflictException;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, UserApiMapperImpl.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, PrivilegeAuthorizationAspect.class})
class RatingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private RateTransactionUseCase rateTransactionUseCase;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    private void authenticateAs(String subject, String... privileges) {
        when(jwtTokenProvider.isValid("good-token")).thenReturn(true);
        Claims claims = Jwts.claims().subject(subject).add("privileges", List.of(privileges)).build();
        when(jwtTokenProvider.parseClaims("good-token")).thenReturn(claims);
    }

    @Test
    void rate_returns401WithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/ratings")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"ratedUserId":"rated-1","transactionType":"ORDER","transactionId":"order-1","score":5}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rate_returns403WithoutPrivilege() throws Exception {
        authenticateAs("rater-1");

        mockMvc.perform(post("/api/v1/ratings")
                        .header("Authorization", "Bearer good-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"ratedUserId":"rated-1","transactionType":"ORDER","transactionId":"order-1","score":5}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void rate_returns201AndUsesJwtSubjectAsRaterId() throws Exception {
        authenticateAs("rater-1", "REPUTATION.RATE");
        when(rateTransactionUseCase.rate("rater-1", "rated-1", "ORDER", "order-1", 5, "Great!"))
                .thenReturn(new RatingResult("rating-1", "ORDER", "order-1", "rater-1", "rated-1", 5, "Great!",
                        Instant.now()));

        mockMvc.perform(post("/api/v1/ratings")
                        .header("Authorization", "Bearer good-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"ratedUserId":"rated-1","transactionType":"ORDER","transactionId":"order-1",
                                 "score":5,"comment":"Great!"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.raterId").value("rater-1"));
    }

    @Test
    void rate_returns409WhenAlreadyRated() throws Exception {
        authenticateAs("rater-1", "REPUTATION.RATE");
        when(rateTransactionUseCase.rate("rater-1", "rated-1", "ORDER", "order-1", 5, null))
                .thenThrow(new ConflictException("RATING_ALREADY_SUBMITTED", "You have already rated this transaction"));

        mockMvc.perform(post("/api/v1/ratings")
                        .header("Authorization", "Bearer good-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"ratedUserId":"rated-1","transactionType":"ORDER","transactionId":"order-1","score":5}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("RATING_ALREADY_SUBMITTED"));
    }
}
