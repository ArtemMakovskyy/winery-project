package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.features.user.dto.UpdateRoleForm;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUiControllerTest {

    @Mock private UserService userService;
    @Mock private Tracer tracer;
    @Mock private Model model;
    @Mock private BindingResult bindingResult;
    @Mock private Span span;

    @InjectMocks
    private UserUiController userUiController;

    @BeforeEach
    void setup() {
        lenient().when(tracer.currentSpan()).thenReturn(span);
    }

    @Test
    void roleForm_ShouldReturnCorrectView() {
        String view = userUiController.roleForm(model);

        assertThat(view).isEqualTo("user/user-role");
        verify(model).addAttribute(eq("form"), any(UpdateRoleForm.class));
    }

    @Test
    void updateRole_WhenValidationErrors_ShouldTagSpanAndReturnView() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = userUiController.updateRole(new UpdateRoleForm(), bindingResult, model);

        assertThat(view).isEqualTo("user/user-role");
        verify(span).tag("validation.status", "failed");
        verifyNoInteractions(userService);
    }

    @Test
    void updateRole_Success_ShouldAddUserToModel() {
        UpdateRoleForm form = new UpdateRoleForm();
        form.setUserId(1L);
        form.setRole("ADMIN");
        UserResponseDto response = new UserResponseDto();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUserRole(1L, "ADMIN")).thenReturn(response);

        String view = userUiController.updateRole(form, bindingResult, model);

        assertThat(view).isEqualTo("user/user-role");
        verify(model).addAttribute("success", true);
        verify(model).addAttribute("user", response);
    }

    @Test
    void updateRole_OnRuntimeException_ShouldAddErrorToModel() {
        UpdateRoleForm form = new UpdateRoleForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUserRole(any(), any())).thenThrow(new RuntimeException("Business error"));

        userUiController.updateRole(form, bindingResult, model);

        verify(span).tag("error.type", "business_logic");
        verify(model).addAttribute("error", "Business error");
    }
}