package com.winestoreapp.wineryadminui.features.user.validation;

import com.winestoreapp.wineryadminui.features.user.dto.RoleName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class UserRoleValidator implements ConstraintValidator<ValidUserRole, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        final RoleName[] validRoles = RoleName.values();

        return Arrays.stream(validRoles)
                .map(Enum::name)
                .anyMatch(validType -> validType.equals(value.toUpperCase()));
    }
}
