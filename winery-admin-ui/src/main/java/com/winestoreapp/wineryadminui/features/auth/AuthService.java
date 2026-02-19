package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.core.observability.ObservationContextualNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserLoginResponseDto;
import io.micrometer.observation.annotation.Observed;
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
    private final SpanTagger spanTagger;

    @Observed(name = ObservationNames.AUTH_SERVICE,
            contextualName = ObservationContextualNames.LOGIN,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.READ})
    public void login(UserLoginRequestDto dto, HttpSession session) {
        UserLoginResponseDto response = authFeignClient.login(dto);
        storage.save(session, response.token());

        spanTagger.tag(ObservationTags.AUTH_STAUS, ObservationTags.SUCCESS);
        log.info("User successfully logged in");
    }

    @Observed(name = ObservationNames.AUTH_SERVICE,
            contextualName = ObservationContextualNames.LOGOUT)
    public void logout(HttpSession session) {
        storage.clear(session);
        spanTagger.tag(ObservationTags.AUTH_STAUS, "logout_success");
    }
}
