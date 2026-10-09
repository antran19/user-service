package com.nexus.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.security.JwtAuthenticationFilter;
import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.security.PrivilegeAuthorizationAspect;
import com.nexus.common.web.GlobalExceptionHandler;
import com.nexus.user.api.mapper.UserApiMapperImpl;
import com.nexus.user.application.exception.DuplicateEmailException;
import com.nexus.user.application.usecase.*;
import com.nexus.user.infrastructure.config.SecurityConfig;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, UserApiMapperImpl.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, JwtTokenProvider.class, PrivilegeAuthorizationAspect.class})
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @MockBean private RegisterUserUseCase registerUserUseCase;
    @MockBean private ChangePasswordUseCase changePasswordUseCase;
    @MockBean private CreateUserUseCase createUserUseCase;
    @MockBean private UpdateUserUseCase updateUserUseCase;
    @MockBean private DeleteUserUseCase deleteUserUseCase;
    @MockBean private GetUserUseCase getUserUseCase;
    @MockBean private ListUsersUseCase listUsersUseCase;
    @MockBean private AdminChangeUserPasswordUseCase adminChangeUserPasswordUseCase;

    private String tokenWith(String... privileges) {
        return jwtTokenProvider.generateToken("admin-1", "ADMIN", List.of(privileges), "TRUSTED");
    }

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

    @Test
    void createUser_returns403WithoutPrivilege() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenWith("PROFILE.VIEW"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"new@example.com","password":"longenough","fullName":"New","roleCode":"SELLER"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_returns201WithPrivilege() throws Exception {
        when(createUserUseCase.create("new@example.com", "longenough", "New", "SELLER"))
                .thenReturn(new UserResult("user-2", "new@example.com", "New", "SELLER", Instant.now()));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenWith("USER.CREATE"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"new@example.com","password":"longenough","fullName":"New","roleCode":"SELLER"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roleCode").value("SELLER"));
    }

    @Test
    void listUsers_returns200WithPrivilege() throws Exception {
        when(listUsersUseCase.list()).thenReturn(
                List.of(new UserResult("user-1", "a@b.com", "A", "BUYER", Instant.now())));

        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + tokenWith("USER.LIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value("a@b.com"));
    }

    @Test
    void getUser_returns404WhenNotFound() throws Exception {
        when(getUserUseCase.get("missing")).thenThrow(new NotFoundException("USER_NOT_FOUND", "User not found: missing"));

        mockMvc.perform(get("/api/v1/users/missing").header("Authorization", "Bearer " + tokenWith("USER.VIEW")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_returns200WithPrivilege() throws Exception {
        mockMvc.perform(delete("/api/v1/users/user-1").header("Authorization", "Bearer " + tokenWith("USER.DELETE")))
                .andExpect(status().isOk());
    }

    @Test
    void adminChangePassword_returns200WithoutNeedingOldPassword() throws Exception {
        mockMvc.perform(put("/api/v1/users/user-1/password")
                        .header("Authorization", "Bearer " + tokenWith("USER.CHANGE_PASSWORD"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"newPassword":"longenough"}"""))
                .andExpect(status().isOk());
    }
}
