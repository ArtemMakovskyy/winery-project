package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.user.dto.UpdateRoleForm;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUiControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private SpanTagger spanTagger;
    @Mock
    private Model model;
    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private UserUiController userUiController;

    @Test
    void roleForm_ShouldReturnCorrectView() {
        String view = userUiController.roleForm(model);

        assertThat(view).isEqualTo("user/user-role");
        verify(model).addAttribute(eq("form"), any(UpdateRoleForm.class));
    }

    @Test
    void updateRole_WhenValidationErrors_ShouldTagAndReturnView() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = userUiController.updateRole(new UpdateRoleForm(), bindingResult, model);

        assertThat(view).isEqualTo("user/user-role");
        verify(spanTagger).tag(ObservationTags.VALIDATION, "failed");
        verifyNoInteractions(userService);
    }

    @Test
    void updateRole_Success_ShouldCallService_AddModel_AndTag() {
        UpdateRoleForm form = new UpdateRoleForm();
        form.setUserId(1L);
        form.setRole("ADMIN");

        UserResponseDto response = new UserResponseDto();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUserRole(1L, "ADMIN")).thenReturn(response);

        String view = userUiController.updateRole(form, bindingResult, model);

        assertThat(view).isEqualTo("user/user-role");

        verify(spanTagger).tag(ObservationTags.USER_ID, 1L);
        verify(spanTagger).tag(ObservationTags.USER_ROLE, "ADMIN");
        verify(spanTagger).tag(ObservationTags.STATUS, ObservationTags.SUCCESS);

        verify(model).addAttribute(ObservationTags.SUCCESS, true);
        verify(model).addAttribute(ObservationTags.USER_ID, response.getId());
    }

    @Test
    void updateRole_RuntimeException_ShouldTagError_AndAddModelError() {
        UpdateRoleForm form = new UpdateRoleForm();
        form.setUserId(1L);
        form.setRole("ADMIN");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUserRole(any(), any()))
                .thenThrow(new RuntimeException("Business error"));

        String view = userUiController.updateRole(form, bindingResult, model);

        assertThat(view).isEqualTo("user/user-role");

        verify(spanTagger).tag(ObservationTags.USER_ID, 1L);
        verify(spanTagger).tag(ObservationTags.USER_ROLE, "ADMIN");
        verify(spanTagger).tag(ObservationTags.ERROR_MESSAGE, ObservationTags.BUSINESS_LOGIC);
        verify(spanTagger).error(any(RuntimeException.class));

        verify(model).addAttribute("error", "Business error");
    }

    @Test
    void updateRole_GenericException_ShouldTagError_AndReturnGenericMessage() {
        UpdateRoleForm form = new UpdateRoleForm();
        form.setUserId(1L);
        form.setRole("ADMIN");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUserRole(any(), any()))
                .thenThrow(new RuntimeException("Unexpected"));

        String view = userUiController.updateRole(form, bindingResult, model);

        assertThat(view).isEqualTo("user/user-role");

        verify(spanTagger).error(any(RuntimeException.class));
    }
}
