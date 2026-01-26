package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.features.user.dto.UpdateUserRoleDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserFeignClient userFeignClient;

    public UserResponseDto updateUserRole(Long userId, String role) {
        log.info("Updating role for user {} to {}", userId, role);
        return userFeignClient.updateUserRole(userId, new UpdateUserRoleDto(role));
    }
}
