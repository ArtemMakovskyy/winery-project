package com.winestoreapp.wineryadminui.features.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserRoleValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserRole {
    String message() default "Invalid user role";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
