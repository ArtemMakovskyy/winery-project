package com.winestoreapp.security.security;

import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.common.observability.ObservationTags;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.user.api.dto.UserLoginRequestDto;
import com.winestoreapp.user.api.dto.UserLoginResponseDto;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SpanTagger spanTagger;

    @Observed(name = ObservationNames.AUTH_AUTHENTICATE)
    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {
        log.info("Authentication attempt for email: {}", request.email());

        spanTagger.tag(ObservationTags.USER_EMAIL, request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            String token = jwtUtil.generateToken(authentication.getName(), authentication.getAuthorities());

            log.info("User {} successfully authenticated", request.email());
            return new UserLoginResponseDto(token);
        } catch (Exception e) {
            log.warn("Failed authentication for user: {}. Reason: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    @Observed(name = ObservationNames.AUTH_LOGOUT)
    public void logout(String token) {
        if (token != null) {
            log.info("Invalidating token for logout");
            jwtUtil.addToInvalidTokens(token);
        }
    }
}
