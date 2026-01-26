package com.winestoreapp.user.service;

import com.winestoreapp.common.config.CustomMySqlContainer;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.user.api.dto.RoleName;
import com.winestoreapp.user.api.dto.UserRegistrationRequestDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.user.config.TestUserConfig;
import com.winestoreapp.user.mapper.UserMapper;
import com.winestoreapp.user.model.Role;
import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.RoleRepository;
import com.winestoreapp.user.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.tracing.Tracer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(classes = TestUserConfig.class)
@ContextConfiguration(initializers = UserServiceImplTest.Initializer.class)
@Transactional
class UserServiceImplTest {
    @Mock
    private Tracer tracer;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserMapper userMapper;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            CustomMySqlContainer container = CustomMySqlContainer.getInstance();
            container.start();

            TestPropertyValues.of(
                    "spring.datasource.url=" + container.getJdbcUrl(),
                    "spring.datasource.username=" + container.getUsername(),
                    "spring.datasource.password=" + container.getPassword(),
                    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                    "spring.jpa.hibernate.ddl-auto=update"
            ).applyTo(context.getEnvironment());
        }
    }

    @Test
    @DisplayName("Register - should save user and return DTO when request is valid")
    void register_ValidRequest_ShouldReturnDto() throws RegistrationException {
        // Prepare roles in DB
        Role roleCustomer = new Role();
        roleCustomer.setName(RoleName.ROLE_CUSTOMER);
        roleRepository.save(roleCustomer);

        UserRegistrationRequestDto request = createRegistrationRequest();
        Mockito.when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        Mockito.when(userMapper.toDto(any(User.class))).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.register(request);

        assertNotNull(result);
        assertTrue(userRepository.findUserByEmail(request.getEmail()).isPresent());
    }

    @Test
    @DisplayName("Register - should throw RegistrationException when email already exists")
    void register_EmailExists_ShouldThrowRegistrationException() {
        User existing = new User();
        existing.setEmail("exists@test.com");
        existing.setFirstName("Ivan");
        existing.setLastName("Ivanov");
        userRepository.save(existing);

        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail("exists@test.com");

        assertThrows(RegistrationException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Get or create - should return existing user and not save new")
    void getOrCreate_UserExists_ShouldReturnExisting() {
        User existing = new User("Ivan", "Petrov");
        userRepository.save(existing);

        Mockito.when(userMapper.toDto(any(User.class))).thenReturn(new UserResponseDto());

        userService.getOrCreateByFirstAndLastName("Ivan", "Petrov");

        long count = userRepository.count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Update role - should throw Exception when trying to change the last admin")
    void updateRole_LastAdmin_ShouldThrowRegistrationException() {
        Role adminRole = new Role();
        adminRole.setName(RoleName.ROLE_ADMIN);
        roleRepository.save(adminRole);

        Role managerRole = new Role();
        managerRole.setName(RoleName.ROLE_MANAGER);
        roleRepository.save(managerRole);

        User lastAdmin = new User();
        lastAdmin.setEmail("admin@test.com");
        lastAdmin.setFirstName("Admin");
        lastAdmin.setLastName("User");
        lastAdmin.setRoles(Set.of(adminRole));
        userRepository.save(lastAdmin);

        assertThrows(RegistrationException.class,
                () -> userService.updateRole(lastAdmin.getId(), "ROLE_MANAGER"));
    }

    @Test
    @DisplayName("Update Telegram Chat ID - should update user record")
    void updateTelegramChatId_ValidId_ShouldUpdate() {
        User user = new User("Ivan", "Petrov");
        user = userRepository.save(user);

        userService.updateTelegramChatId(user.getId(), 999L);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(999L, updated.getTelegramChatId());
    }

    @Test
    @DisplayName("Load by email - should throw EntityNotFoundException when user missing")
    void loadUserByEmail_NotFound_ShouldThrowEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> userService.loadUserByEmail("missing@test.com"));
    }

    private UserRegistrationRequestDto createRegistrationRequest() {
        UserRegistrationRequestDto dto = new UserRegistrationRequestDto();
        dto.setEmail("test@email.com");
        dto.setPassword("password123");
        dto.setRepeatPassword("password123");
        dto.setFirstName("Ivan");
        dto.setLastName("Petrov");
        dto.setPhoneNumber("+380991234567");
        return dto;
    }
}
