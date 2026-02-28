package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.util.FeignErrorParser;
import com.winestoreapp.wineryadminui.features.user.dto.UpdateUserRoleDto;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


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
    private SpanTagger spanTagger;

    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserService(userFeignClient, errorParser, spanTagger);
    }

    @Test
    void updateUserRole_success() {
        UserResponseDto response = new UserResponseDto();

        when(userFeignClient.updateUserRole(eq(1L), any(UpdateUserRoleDto.class)))
                .thenReturn(response);

        UserResponseDto result = service.updateUserRole(1L, "ROLE_ADMIN");

        assertThat(result).isNotNull();

        verify(spanTagger).tag(ObservationTags.USER_ID, 1L);
        verify(spanTagger).tag(ObservationTags.USER_ROLE, "ROLE_ADMIN");
        verify(spanTagger).tag(ObservationTags.STATUS, "success");

        verify(userFeignClient)
                .updateUserRole(eq(1L), any(UpdateUserRoleDto.class));
    }

    @Test
    void updateUserRole_feignError_shouldThrowRuntimeException() {
        FeignException feignException = mock(FeignException.class);

        when(userFeignClient.updateUserRole(eq(1L), any(UpdateUserRoleDto.class)))
                .thenThrow(feignException);
        when(errorParser.extractMessage(feignException))
                .thenReturn("Invalid role");

        assertThatThrownBy(() -> service.updateUserRole(1L, "ROLE_FAKE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid role");

        verify(spanTagger).tag(ObservationTags.USER_ID, 1L);
        verify(spanTagger).tag(ObservationTags.USER_ROLE, "ROLE_FAKE");
        verify(spanTagger).tag(ObservationTags.STATUS, "error");
        verify(spanTagger).error(feignException);

        verify(errorParser).extractMessage(feignException);
    }
}
