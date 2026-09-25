package com.nexus.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.user.api.mapper.UserApiMapperImpl;
import com.nexus.user.application.exception.DuplicateEmailException;
import com.nexus.user.application.usecase.ChangePasswordUseCase;
import com.nexus.user.application.usecase.RegisterUserUseCase;
import com.nexus.user.application.usecase.UserRegistrationResult;
import com.nexus.user.infrastructure.config.SecurityConfig;
import com.nexus.common.security.JwtAuthenticationFilter;
import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, UserApiMapperImpl.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, JwtTokenProvider.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private ChangePasswordUseCase changePasswordUseCase;

    @Test
    void register_returns201WithCreatedUser() throws Exception {
        when(registerUserUseCase.register(any()))
                .thenReturn(new UserRegistrationResult("user-1", "alice@example.com", "Alice Nguyen"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"longenough","fullName":"Alice Nguyen"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    void register_returns409WhenEmailTaken() throws Exception {
        when(registerUserUseCase.register(any()))
                .thenThrow(new DuplicateEmailException("alice@example.com"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"longenough","fullName":"Alice Nguyen"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void register_returns400WhenEmailBlank() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"","password":"longenough","fullName":"Alice Nguyen"}"""))
                .andExpect(status().isBadRequest());
    }
}
