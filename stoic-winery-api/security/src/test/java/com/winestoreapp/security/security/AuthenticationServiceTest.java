package com.winestoreapp.security.security;

import com.winestoreapp.user.api.dto.UserLoginRequestDto;
import com.winestoreapp.user.api.dto.UserLoginResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import io.micrometer.tracing.Tracer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private Tracer tracer;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_ValidRequest_ReturnsToken() {
        UserLoginRequestDto request = new UserLoginRequestDto("test@mail.com", "password");
        Authentication auth = new UsernamePasswordAuthenticationToken(request.email(), null);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(auth.getName(), auth.getAuthorities())).thenReturn("mock-token");

        UserLoginResponseDto response = authenticationService.authenticate(request);

        assertEquals("mock-token", response.token());
    }

    @Test
    void logout_ValidToken_CallsBlacklist() {
        String token = "some-token";
        authenticationService.logout(token);
        verify(jwtUtil).addToInvalidTokens(token);
    }
}
