package com.winestoreapp.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.common.exception.CustomGlobalExceptionHandler;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.security.config.AuthenticationControllerTestConfig;
import com.winestoreapp.security.security.AuthenticationService;
import com.winestoreapp.security.security.JwtUtil;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UserLoginRequestDto;
import com.winestoreapp.user.api.dto.UserLoginResponseDto;
import com.winestoreapp.user.api.dto.UserRegistrationRequestDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
        AuthenticationControllerTestConfig.class,
        CustomGlobalExceptionHandler.class
})
class AuthenticationControllerTest {
    @MockBean
    private SpanTagger spanTagger;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /auth/login - Success")
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("testuser@email.com", "password123");
        UserLoginResponseDto responseDto = new UserLoginResponseDto("mocked-jwt-token");

        when(authenticationService.authenticate(any(UserLoginRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/register - Success")
    void register_ValidDto_ShouldReturnCreated() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("newuser@email.com");
        requestDto.setFirstName("Ivan");
        requestDto.setLastName("Ivanov");
        requestDto.setPhoneNumber("+380501234567");
        requestDto.setPassword("password123");
        requestDto.setRepeatPassword("password123");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail("newuser@email.com");
        responseDto.setRoles(Set.of("ROLE_CUSTOMER"));

        when(userService.register(any(UserRegistrationRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@email.com"));
    }

    @Test
    @DisplayName("POST /auth/logout - Success")
    void logout_WithToken_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Logout successful"));
    }

    @Test
    @DisplayName("POST /auth/register - Validation Failure (Short Password)")
    void register_InvalidDto_ShouldReturnBadRequest() throws Exception {
        UserRegistrationRequestDto invalidDto = new UserRegistrationRequestDto();
        invalidDto.setEmail("test@email.com");
        invalidDto.setPassword("123");

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
