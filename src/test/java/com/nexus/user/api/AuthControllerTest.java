package com.nexus.user.api;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.web.GlobalExceptionHandler;
import com.nexus.user.application.exception.InvalidCredentialsException;
import com.nexus.user.application.exception.InvalidResetTokenException;
import com.nexus.user.application.usecase.ForgotPasswordUseCase;
import com.nexus.user.application.usecase.LoginResult;
import com.nexus.user.application.usecase.LoginUseCase;
import com.nexus.user.application.usecase.LogoutUseCase;
import com.nexus.user.application.usecase.ResetPasswordUseCase;
import com.nexus.user.infrastructure.config.SecurityConfig;
import com.nexus.common.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private LoginUseCase loginUseCase;

    @MockBean
    private LogoutUseCase logoutUseCase;

    @MockBean
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @MockBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @Test
    void login_returns200WithToken() throws Exception {
        when(loginUseCase.login(any())).thenReturn(new LoginResult("signed-jwt", "user-1"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"longenough"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("signed-jwt"));
    }

    @Test
    void login_returns401ForInvalidCredentials() throws Exception {
        when(loginUseCase.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"wrong"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void logout_returns200AndBlacklistsTheBearerToken() throws Exception {
        String token = jwtTokenProvider.generateToken("user-1", "BUYER", List.of("AUTH.LOGIN"), "TRUSTED");

        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(logoutUseCase).logout(token);
    }

    @Test
    void logout_returns401WithoutAToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPassword_returns200WithResetToken() throws Exception {
        when(forgotPasswordUseCase.requestReset("carol@example.com")).thenReturn("raw-reset-token");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"carol@example.com"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetToken").value("raw-reset-token"));
    }

    @Test
    void resetPassword_returns200WhenTokenIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"token":"raw-reset-token","newPassword":"newlongpassword"}"""))
                .andExpect(status().isOk());

        verify(resetPasswordUseCase).resetPassword("raw-reset-token", "newlongpassword");
    }

    @Test
    void resetPassword_returns401ForInvalidToken() throws Exception {
        doThrow(new InvalidResetTokenException()).when(resetPasswordUseCase).resetPassword(any(), any());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"token":"bad-token","newPassword":"newlongpassword"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_RESET_TOKEN"));
    }
}
