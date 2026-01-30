package com.winestoreapp.wineryadminui.features.user.dto;

import com.winestoreapp.wineryadminui.features.user.validation.ValidUserRole;

public record UpdateUserRoleDto(
        @ValidUserRole
        String role) {
}
