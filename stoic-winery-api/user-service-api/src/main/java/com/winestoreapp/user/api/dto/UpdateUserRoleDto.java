package com.winestoreapp.user.api.dto;

import com.winestoreapp.user.api.validation.ValidUserRole;

public record UpdateUserRoleDto(
        @ValidUserRole
        String role) {
}
