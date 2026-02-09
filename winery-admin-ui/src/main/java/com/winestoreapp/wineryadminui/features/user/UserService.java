package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.user.dto.UpdateUserRoleDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserFeignClient userFeignClient;
    private final FeignErrorParser errorParser;
    private final Tracer tracer;

    @Observed(name = "user.service.update_role")
    public UserResponseDto updateUserRole(Long userId, String role) {
        log.info("Action: Updating role for UserID: {} to {}", userId, role);

        tagSpan("target.user.id", userId);
        tagSpan("target.role", role);

        try {
            UserResponseDto response = userFeignClient.updateUserRole(userId, new UpdateUserRoleDto(role));
            log.info("Successfully updated role for UserID: {}", userId);

            tagSpan("status", "success");
            return response;
        } catch (FeignException e) {
            String extractedMessage = errorParser.extractMessage(e);
            log.warn("Role update failed for UserID {}: {}", userId, extractedMessage);

            tagSpan("status", "error");
            recordError(e);

            throw new RuntimeException(extractedMessage);
        }
    }

    private void tagSpan(String key, Object value) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    private void recordError(Throwable e) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.error(e);
        }
    }
}