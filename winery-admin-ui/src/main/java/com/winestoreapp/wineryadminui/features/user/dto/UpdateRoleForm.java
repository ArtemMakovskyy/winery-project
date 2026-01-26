package com.winestoreapp.wineryadminui.features.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleForm {
    private Long userId;
    private String role;
}
