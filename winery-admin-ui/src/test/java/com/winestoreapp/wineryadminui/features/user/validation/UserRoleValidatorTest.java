package com.winestoreapp.wineryadminui.features.user.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UserRoleValidatorTest {

    private final UserRoleValidator validator = new UserRoleValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @ParameterizedTest
    @ValueSource(strings = {
            "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CUSTOMER", // Upper case
            "role_admin", "role_manager", "role_customer", // Lower case
            "Role_Admin", "ROLE_manager"                    // Mixed case
    })
    void shouldAcceptValidRolesIgnoringCase(String role) {
        assertThat(validator.isValid(role, context)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {
            "ADMIN", "MANAGER", "USER",    // Missing ROLE_ prefix
            "INVALID_ROLE", "ROLE_",       // Incomplete or wrong
            "RANDOM_TEXT"
    })
    void shouldRejectInvalidRoles(String role) {
        assertThat(validator.isValid(role, context)).isFalse();
    }
}
