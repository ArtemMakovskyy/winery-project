package com.winestoreapp.user;

import com.winestoreapp.exception.EntityNotFoundException;
import com.winestoreapp.exception.RegistrationException;
import com.winestoreapp.user.dto.UserRegistrationRequestDto;
import com.winestoreapp.user.dto.UserResponseDto;
import com.winestoreapp.user.mapper.UserMapper;
import com.winestoreapp.user.model.Role;
import com.winestoreapp.user.model.RoleName;
import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.RoleRepository;
import com.winestoreapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final int MINIMUM_ALLOWED_NUMBER_OF_ADMIN_USERS = 1;
    private static final long CUSTOMER_ROLE_ID = 3L;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException("Unable to complete registration.");
        }

        Role roleCustomer = roleRepository.findById(CUSTOMER_ROLE_ID).orElseThrow(
                () -> new EntityNotFoundException("Can't find ROLE_CUSTOMER by id " + CUSTOMER_ROLE_ID));

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                Set.of(roleCustomer)
        );

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto updateRole(Long userId, String roleNameStr) {
        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id " + userId));

        Role roleFromDb = roleRepository.findByName(RoleName.valueOf(roleNameStr)).orElseThrow(
                () -> new EntityNotFoundException("Can't find role " + roleNameStr));

        if (userFromDb.hasRole(RoleName.ROLE_ADMIN)) {
            List<User> adminsInDb = userRepository.findUsersByRole(RoleName.ROLE_ADMIN);
            if (adminsInDb.size() <= MINIMUM_ALLOWED_NUMBER_OF_ADMIN_USERS) {
                throw new RegistrationException("You cannot change the last admin role.");
            }
        }

        userFromDb.updateRoles(Set.of(roleFromDb));
        return userMapper.toDto(userRepository.save(userFromDb));
    }
}
