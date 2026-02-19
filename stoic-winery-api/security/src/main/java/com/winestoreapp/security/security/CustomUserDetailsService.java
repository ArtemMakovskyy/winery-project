package com.winestoreapp.security.security;

import com.winestoreapp.common.observability.ObservationContextualNames;
import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import io.micrometer.observation.annotation.Observed;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Observed(name = ObservationNames.AUTH_SERVICE, contextualName = ObservationContextualNames.LOAD_USER_DETAILS)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username)
                .orElseThrow(() -> {
                    log.error("User details not found for email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }
}
