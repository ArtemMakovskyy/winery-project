package com.winestoreapp.security.security;

import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.user.repository.UserRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Observed(name = ObservationNames.AUTH_LOAD_USER)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username)
                .orElseThrow(() -> {
                    log.error("User details not found for email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }
}
