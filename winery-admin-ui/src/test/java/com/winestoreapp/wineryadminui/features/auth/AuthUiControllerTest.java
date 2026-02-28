package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUiControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @InjectMocks
    private AuthUiController authUiController;

    @BeforeEach
    void setUp() {
        lenient().when(tracer.currentSpan()).thenReturn(span);
    }

    @Test
    void loginPage_ShouldReturnViewAndAddDto() {
        String view = authUiController.loginPage(model);

        assertThat(view).isEqualTo("auth/login");
        verify(model).addAttribute(eq("loginDto"), any(UserLoginRequestDto.class));
    }

    @Test
    void doLogin_WhenValidationErrors_ShouldReturnLoginView() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = authUiController.doLogin(new UserLoginRequestDto("", ""), bindingResult, session, model);

        assertThat(view).isEqualTo("auth/login");
        verifyNoInteractions(authService);
    }

    @Test
    void doLogin_OnSuccess_ShouldRedirectToDashboard() throws Exception {
        UserLoginRequestDto dto = new UserLoginRequestDto("test@mail.com", "pass");
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = authUiController.doLogin(dto, bindingResult, session, model);

        assertThat(view).isEqualTo("redirect:/ui/dashboard");
        verify(authService).login(dto, session);
    }

    @Test
    void doLogin_OnAuthException_ShouldReturnLoginWithError() throws Exception {
        UserLoginRequestDto dto = new UserLoginRequestDto("test@mail.com", "wrong");
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("Auth failed")).when(authService).login(dto, session);

        String view = authUiController.doLogin(dto, bindingResult, session, model);

        assertThat(view).isEqualTo("auth/login");
        verify(model).addAttribute("error", "Invalid email or password");
    }

    @Test
    void logout_OnSuccess_ShouldRedirectWithFlashMessage() throws Exception {
        String view = authUiController.logout(session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/login");
        verify(authService).logout(session);
        verify(redirectAttributes).addFlashAttribute("message", "Successfully logged out");
    }

    @Test
    void logout_OnException_ShouldRedirectWithErrorMessage() throws Exception {
        doThrow(new RuntimeException("Logout error")).when(authService).logout(session);

        String view = authUiController.logout(session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/login");
        verify(redirectAttributes).addFlashAttribute("error", "Logout failed");
    }
}
