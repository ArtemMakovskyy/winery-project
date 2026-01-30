package com.winestoreapp.wineryadminui.features.user.dto;

import com.winestoreapp.wineryadminui.features.user.validation.ValidUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleForm {
    @NotNull(message = "User ID cannot be empty")
    private Long userId;

    @NotBlank(message = "Role cannot be blank")
    @ValidUserRole
    private String role;
}