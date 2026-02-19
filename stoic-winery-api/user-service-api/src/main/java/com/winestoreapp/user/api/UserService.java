package com.winestoreapp.user.api;

import com.winestoreapp.user.api.dto.RoleName;
import com.winestoreapp.user.api.dto.UserRegistrationRequestDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDto findUserByEmail(String email);

    UserResponseDto loadUserById(Long id);

    UserResponseDto getOrCreateByFirstAndLastName(String firstName, String lastName);

    UserResponseDto getOrUpdateOrCreateUser(String email, String fName, String lName, String phone);

    List<UserResponseDto> findUsersByRole(RoleName role);

    Optional<UserResponseDto> findUserByTelegramChatId(Long chatId);

    void updateTelegramChatId(Long userId, Long chatId);

    UserResponseDto register(UserRegistrationRequestDto request);

    UserResponseDto updateRole(Long userId, String role);
}
