package com.winestoreapp.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.common.exception.CustomGlobalExceptionHandler;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UpdateUserRoleDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.user.config.UserControllerTestConfig;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {
        UserControllerTestConfig.class,
        CustomGlobalExceptionHandler.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setEmail("test@email.com");
        userResponseDto.setFirstName("John");
        userResponseDto.setLastName("Doe");
        userResponseDto.setRoles(Set.of("ROLE_CUSTOMER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /users/{id}/role - Success")
    void updateRole_ValidData_ShouldReturnUpdatedUser() throws Exception {
        UpdateUserRoleDto updateDto = new UpdateUserRoleDto("ROLE_MANAGER");
        userResponseDto.setRoles(Set.of("ROLE_MANAGER"));

        when(userService.updateRole(eq(1L), eq("ROLE_MANAGER"))).thenReturn(userResponseDto);

        mockMvc.perform(put("/users/1/role")
                        .with(csrf()) // Добавлено, так как PUT требует CSRF в защищенных тестах
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_MANAGER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /users/{id}/role - Invalid Role Name")
    void updateRole_InvalidRole_ShouldReturnBadRequest() throws Exception {
        UpdateUserRoleDto updateDto = new UpdateUserRoleDto("INVALID");

        mockMvc.perform(put("/users/1/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /users/{id}/role - Forbidden (No Auth)")
    void updateRole_NoAuth_ShouldReturnForbidden() throws Exception {
        UpdateUserRoleDto updateDto = new UpdateUserRoleDto("ROLE_MANAGER");

        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}
