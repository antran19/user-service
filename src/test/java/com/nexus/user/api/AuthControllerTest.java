package com.nexus.user.api;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.web.GlobalExceptionHandler;
import com.nexus.user.application.exception.InvalidCredentialsException;
import com.nexus.user.application.usecase.LoginResult;
import com.nexus.user.application.usecase.LoginUseCase;
import com.nexus.user.infrastructure.config.SecurityConfig;
import com.nexus.common.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginUseCase loginUseCase;

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
}
