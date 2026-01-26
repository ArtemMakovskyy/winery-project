package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthFeignClient authFeignClient;
    private final SessionTokenStorage storage;

    public void login(UserLoginRequestDto dto, HttpSession session) {
        UserLoginResponseDto response = authFeignClient.login(dto);
        storage.save(session, response.token());
    }

    public void logout(HttpSession session) {
        storage.clear(session);
    }
}
