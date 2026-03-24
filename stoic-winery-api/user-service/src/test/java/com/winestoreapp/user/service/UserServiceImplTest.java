package com.winestoreapp.user.service;

import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.user.api.dto.RoleName;
import com.winestoreapp.user.api.dto.UserRegistrationRequestDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.user.mapper.UserMapper;
import com.winestoreapp.user.model.Role;
import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.RoleRepository;
import com.winestoreapp.user.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SpanTagger spanTagger;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_ValidRequest_ShouldReturnDto() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail("test@test.com");
        request.setPassword("1234");

        Role role = new Role();
        role.setName(RoleName.ROLE_CUSTOMER);

        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        User saved = new User();
        saved.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userMapper.toDto(any(User.class))).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.register(request);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailExists_ShouldThrow() {
        when(userRepository.findUserByEmail(anyString()))
                .thenReturn(Optional.of(new User()));

        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail("exists@test.com");

        assertThrows(RegistrationException.class,
                () -> userService.register(request));
    }

    @Test
    void findUserByEmail_NotFound_ShouldThrow() {
        when(userRepository.findUserByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.findUserByEmail("missing@test.com"));
    }

    @Test
    void updateTelegramChatId_ShouldUpdate() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateTelegramChatId(1L, 999L);

        assertEquals(999L, user.getTelegramChatId());
        verify(userRepository).save(user);
    }

    @Test
    void updateRole_LastAdmin_ShouldThrow() {
        Role admin = new Role();
        admin.setName(RoleName.ROLE_ADMIN);

        Role manager = new Role();
        manager.setName(RoleName.ROLE_MANAGER);

        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of(admin));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_MANAGER)).thenReturn(Optional.of(manager));
        when(userRepository.findUsersByRole(RoleName.ROLE_ADMIN))
                .thenReturn(java.util.List.of(user));

        assertThrows(RegistrationException.class,
                () -> userService.updateRole(1L, "ROLE_MANAGER"));
    }
}