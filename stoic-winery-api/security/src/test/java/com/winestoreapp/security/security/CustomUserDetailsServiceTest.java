package com.winestoreapp.security.security;

import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        String email = "test@mail.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername(email);
        assertEquals(email, result.getUsername());
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        String email = "notfound@mail.com";
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email));
    }
}
