package com.winestoreapp.user.controller;

import com.winestoreapp.common.dto.ResponseErrorDto;
import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.common.observability.ObservationTags;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UpdateUserRoleDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PATCH, RequestMethod.DELETE})
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final SpanTagger spanTagger;

    @Operation(summary = "Update user role by user id",
            description = """
                    Update user role by user identification number. Only for ADMIN.
                    Available roles: ROLE_CUSTOMER, ROLE_MANAGER, ROLE_ADMIN.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid role name",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Data for updating user role",
            content = @Content(
                    schema = @Schema(implementation = UpdateUserRoleDto.class),
                    examples = @ExampleObject(
                            name = "Role update example",
                            value = """
                            {
                              "role": "ROLE_MANAGER"
                            }
                            """
                    )
            )
    )
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Observed(name = ObservationNames.USER_UPDATE_ROLE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateUserRoleDto roleDto
    ) {

        log.info("ADMIN request to update role for user ID: {} to {}", id, roleDto.role());

        spanTagger.tag(ObservationTags.USER_ID, id);
        spanTagger.tag(ObservationTags.USER_ROLE, roleDto.role());

        return ResponseEntity.ok(userService.updateRole(id, roleDto.role()));
    }

}