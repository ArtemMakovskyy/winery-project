package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.features.user.dto.UpdateUserRoleDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "userFeignClient",
        url = "${api.backend-url}"
)
public interface UserFeignClient {

    @PutMapping("/users/{id}/role")
    UserResponseDto updateUserRole(
            @PathVariable("id") Long id,
            @RequestBody UpdateUserRoleDto roleDto
    );

}
