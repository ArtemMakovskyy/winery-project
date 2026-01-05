package com.winestoreapp.user;

import com.winestoreapp.user.dto.UserRegistrationRequestDto;
import com.winestoreapp.user.dto.UserResponseDto;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto request);

    UserResponseDto updateRole(Long userId, String role);
}
