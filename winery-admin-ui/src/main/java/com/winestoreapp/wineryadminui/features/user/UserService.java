package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.core.observability.ObservationContextualNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.user.dto.UpdateUserRoleDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import feign.FeignException;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserFeignClient userFeignClient;
    private final FeignErrorParser errorParser;
    private final SpanTagger spanTagger;

    @Observed(name = ObservationNames.USER_SERVICE,
            contextualName = ObservationContextualNames.UPDATE_ROLE)
    public UserResponseDto updateUserRole(Long userId, String role) {
        log.info("Action: Updating role for UserID: {} to {}", userId, role);
        spanTagger.tag(ObservationTags.USER_ID, userId);
        spanTagger.tag(ObservationTags.USER_ROLE, role);

        try {
            UserResponseDto response = userFeignClient.updateUserRole(userId, new UpdateUserRoleDto(role));
            log.info("Successfully updated role for UserID: {}", userId);

            spanTagger.tag(ObservationTags.STATUS, "success");

            return response;
        } catch (FeignException e) {
            String extractedMessage = errorParser.extractMessage(e);
            log.warn("Role update failed for UserID {}: {}", userId, extractedMessage);

            spanTagger.tag(ObservationTags.STATUS, "error");
            spanTagger.error(e);

            throw new RuntimeException(extractedMessage);
        }
    }
}
