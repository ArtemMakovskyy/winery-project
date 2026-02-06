package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.user.dto.UpdateUserRoleDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import feign.FeignException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private FeignErrorParser errorParser;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(tracer.currentSpan()).thenReturn(span);
        service = new UserService(userFeignClient, errorParser, tracer);
    }

    @Test
    void updateUserRole_success() {
        UserResponseDto response = new UserResponseDto();
        when(userFeignClient.updateUserRole(eq(1L), any(UpdateUserRoleDto.class)))
                .thenReturn(response);

        UserResponseDto result = service.updateUserRole(1L, "ROLE_ADMIN");

        assertThat(result).isNotNull();
        verify(userFeignClient).updateUserRole(eq(1L), any(UpdateUserRoleDto.class));
        verify(span).tag("target.user.id", "1");
        verify(span).tag("target.role", "ROLE_ADMIN");
    }

    @Test
    void updateUserRole_feignError_shouldThrowRuntimeException() {
        FeignException feignException = mock(FeignException.class);
        when(userFeignClient.updateUserRole(eq(1L), any(UpdateUserRoleDto.class)))
                .thenThrow(feignException);
        when(errorParser.extractMessage(feignException)).thenReturn("Invalid role");

        assertThatThrownBy(() -> service.updateUserRole(1L, "ROLE_FAKE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid role");

        verify(errorParser).extractMessage(feignException);
    }
}
