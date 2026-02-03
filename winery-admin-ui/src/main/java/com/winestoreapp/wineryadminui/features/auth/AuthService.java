package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthFeignClient authFeignClient;
    private final SessionTokenStorage storage;

    public void login(UserLoginRequestDto dto, HttpSession session) {
        log.info("Attempting login for user: {}", dto.email());
        UserLoginResponseDto response = authFeignClient.login(dto);
        storage.save(session, response.token());
        log.info("User {} successfully authenticated. Session ID: {}", dto.email(), session.getId());
    }

    public void logout(HttpSession session) {
        log.info("Logging out session: {}", session.getId());
        storage.clear(session);
    }
}