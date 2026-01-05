package com.winestoreapp.user.mapper;

import com.winestoreapp.config.MapperConfig;
import com.winestoreapp.user.dto.UserResponseDto;
import com.winestoreapp.user.model.Role;
import com.winestoreapp.user.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponseDto toDto(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    @AfterMapping
    default void setUserRoles(@MappingTarget UserResponseDto userDto, User user) {
        userDto.setRoles(mapRoles(user.getRoles()));
    }
}
