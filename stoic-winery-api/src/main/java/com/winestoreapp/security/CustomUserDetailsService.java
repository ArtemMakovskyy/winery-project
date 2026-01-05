package com.winestoreapp.security;

import com.winestoreapp.exception.EntityNotFoundException;
import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findUserByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user be email "));
        return user;
    }
}
