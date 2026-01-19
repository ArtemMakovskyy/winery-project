package com.winestoreapp.user.service;

import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.RoleName;
import com.winestoreapp.user.api.dto.UserRegistrationRequestDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.user.mapper.UserMapper;
import com.winestoreapp.user.model.Role;
import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.RoleRepository;
import com.winestoreapp.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed(name = "user.service")
public class UserServiceImpl implements UserService {
    private static final int MINIMUM_ALLOWED_NUMBER_OF_ADMIN_USERS = 1;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final Tracer tracer;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto loadUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user by email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto loadUserById(Long id) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("user.id", String.valueOf(id));
        }
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user by id: " + id));
    }

    @Override
    @Transactional
    public UserResponseDto getOrCreateByFirstAndLastName(String firstName, String lastName) {
        User user = userRepository.findFirstByFirstNameAndLastName(firstName, lastName)
                .orElseGet(() -> {
                    log.info("Creating new user for names: {} {}", firstName, lastName);
                    User newUser = new User(firstName, lastName);
                    return userRepository.save(newUser);
                });
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto getOrUpdateOrCreateUser(String email, String fName, String lName, String phone) {
        User user = userRepository.findUserByEmail(email)
                .or(() -> userRepository.findFirstByFirstNameAndLastName(fName, lName))
                .map(existingUser -> {
                    log.debug("Updating existing user info for email: {}", email);
                    existingUser.setPhoneNumber(phone);
                    existingUser.setEmail(email);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    log.info("Creating new customer user with email: {}", email);
                    User newUser = new User(email, fName, lName, phone);
                    roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                            .ifPresent(role -> newUser.setRoles(Set.of(role)));
                    return userRepository.save(newUser);
                });
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findUsersByRole(final RoleName role) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("user.role", role.name());
        }
        return userRepository.findUsersByRole(role).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findUserByTelegramChatId(final Long chatId) {
        return userRepository.findUserByTelegramChatId(chatId)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public void updateTelegramChatId(final Long userId, final Long chatId) {
        log.info("Updating telegram chatId for userId: {}", userId);
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("user.id", String.valueOf(userId));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user by id: " + userId));
        user.setTelegramChatId(chatId);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: email {} already exists", request.getEmail());
            throw new RegistrationException("Unable to complete registration.");
        }

        log.info("Registering new user with email: {}", request.getEmail());

        Role roleCustomer = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("Can't find ROLE_CUSTOMER"));

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                new HashSet<>(Set.of(roleCustomer))
        );

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto updateRole(Long userId, String roleNameStr) {
        log.info("Attempting to update role for userId: {} to {}", userId, roleNameStr);
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("user.id", String.valueOf(userId));
            tracer.currentSpan().tag("new.role", roleNameStr);
        }

        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id " + userId));

        Role roleFromDb = roleRepository.findByName(RoleName.valueOf(roleNameStr)).orElseThrow(
                () -> new EntityNotFoundException("Can't find role " + roleNameStr));

        if (userFromDb.hasRole(RoleName.ROLE_ADMIN)) {
            List<User> adminsInDb = userRepository.findUsersByRole(RoleName.ROLE_ADMIN);
            if (adminsInDb.size() <= MINIMUM_ALLOWED_NUMBER_OF_ADMIN_USERS) {
                log.error("Security violation: attempt to remove last admin role from userId: {}", userId);
                throw new RegistrationException("You cannot change the last admin role.");
            }
        }

        userFromDb.updateRoles(new HashSet<>(Set.of(roleFromDb)));
        log.info("Role updated successfully for userId: {}", userId);

        return userMapper.toDto(userRepository.save(userFromDb));
    }
}
