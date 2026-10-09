package com.nexus.user.api;

import com.nexus.common.security.JwtAuthenticationFilter;
import com.nexus.common.security.JwtTokenProvider;
import com.nexus.common.security.PrivilegeAuthorizationAspect;
import com.nexus.common.web.GlobalExceptionHandler;
import com.nexus.user.api.mapper.UserApiMapperImpl;
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

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, UserApiMapperImpl.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, JwtTokenProvider.class, PrivilegeAuthorizationAspect.class})
class RoleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @MockBean private CreateRoleUseCase createRoleUseCase;
    @MockBean private UpdateRoleUseCase updateRoleUseCase;
    @MockBean private DeleteRoleUseCase deleteRoleUseCase;
    @MockBean private ListRolesUseCase listRolesUseCase;
    @MockBean private GetRoleUseCase getRoleUseCase;

    private String tokenWith(String... privileges) {
        return jwtTokenProvider.generateToken("admin-1", "ADMIN", List.of(privileges), "TRUSTED");
    }

    @Test
    void create_returns403WithoutPrivilege() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + tokenWith("PROFILE.VIEW"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code":"MODERATOR","name":"Moderator","privilegeCodes":[]}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_returns201WithPrivilege() throws Exception {
        when(createRoleUseCase.create("MODERATOR", "Moderator", Set.of()))
                .thenReturn(new RoleResult("role-1", "MODERATOR", "Moderator", Set.of()));

        mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + tokenWith("ROLE.CREATE"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"code":"MODERATOR","name":"Moderator","privilegeCodes":[]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("MODERATOR"));
    }

    @Test
    void list_returns200WithPrivilege() throws Exception {
        when(listRolesUseCase.list()).thenReturn(List.of(new RoleResult("role-1", "BUYER", "Buyer", Set.of())));

        mockMvc.perform(get("/api/v1/roles").header("Authorization", "Bearer " + tokenWith("ROLE.LIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("BUYER"));
    }

    @Test
    void delete_returns200WithPrivilege() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/role-1").header("Authorization", "Bearer " + tokenWith("ROLE.DELETE")))
                .andExpect(status().isOk());
    }
}
