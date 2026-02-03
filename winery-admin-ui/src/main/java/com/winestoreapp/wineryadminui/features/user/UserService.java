package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.user.dto.UpdateUserRoleDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import feign.FeignException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserFeignClient userFeignClient;
    private final FeignErrorParser errorParser;

    public UserResponseDto updateUserRole(Long userId, String role) {
        log.info("Action: Updating role for UserID: {} to {}", userId, role);
        try {
            UserResponseDto response = userFeignClient.updateUserRole(userId, new UpdateUserRoleDto(role));
            log.info("Successfully updated role for UserID: {}", userId);
            return response;
        } catch (FeignException e) {
            String extractedMessage = errorParser.extractMessage(e);
            log.warn("Role update failed for UserID {}: {}", userId, extractedMessage);
            throw new RuntimeException(extractedMessage);
        }
    }
}