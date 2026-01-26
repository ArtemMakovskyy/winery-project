package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserRegistrationRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "authClient",
        url = "${api.backend-url}/auth"
)
public interface AuthFeignClient {

    @PostMapping("/login")
    UserLoginResponseDto login(@RequestBody UserLoginRequestDto request);

    @PostMapping("/logout")
    void logout();

    @PostMapping("/register")
    UserResponseDto registerUser(@RequestBody UserRegistrationRequestDto requestDto);
}
