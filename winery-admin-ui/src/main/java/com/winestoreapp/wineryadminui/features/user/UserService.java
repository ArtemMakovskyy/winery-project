package com.winestoreapp.wineryadminui.features.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.wineryadminui.features.user.dto.BackendErrorResponse;
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
    private final ObjectMapper objectMapper;

    public UserResponseDto updateUserRole(Long userId, String role) {
        log.info("Updating role for user {} to {}", userId, role);
        try {
            return userFeignClient.updateUserRole(userId, new UpdateUserRoleDto(role));
        } catch (FeignException e) {
            String extractedMessage = extractErrorMessage(e);
            log.warn("Backend returned error: {}", extractedMessage);
            throw new RuntimeException(extractedMessage);
        }
    }

    private String extractErrorMessage(FeignException e) {
        try {
            if (e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
                BackendErrorResponse error = objectMapper.readValue(e.contentUTF8(), BackendErrorResponse.class);
                return error.message();
            }
        } catch (Exception ex) {
            log.error("Failed to parse backend error", ex);
        }
        return "Internal server error";
    }
}
