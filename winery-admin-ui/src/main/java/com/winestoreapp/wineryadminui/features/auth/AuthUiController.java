package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.micrometer.observation.annotation.Observed;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthUiController {

    private final AuthService authService;

    @GetMapping("/login")
    @Observed(name = "ui.login.page")
    public String loginPage(Model model) {
        model.addAttribute("loginDto", new UserLoginRequestDto("", ""));
        return "auth/login";
    }

    @PostMapping("/login")
    @Observed(name = "ui.login.submit")
    public String doLogin(
            @Valid @ModelAttribute("loginDto") UserLoginRequestDto dto,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Login validation failed for email: {}", dto.email());
            return "auth/login";
        }

        try {
            authService.login(dto, session);
            return "redirect:/ui/dashboard";
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", dto.email(), e);
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    @Observed(name = "ui.logout.submit")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            authService.logout(session);
            redirectAttributes.addFlashAttribute("message", "Successfully logged out");
        } catch (Exception e) {
            log.error("Logout failed for session: {}", session.getId(), e);
            redirectAttributes.addFlashAttribute("error", "Logout failed");
        }
        return "redirect:/login";
    }
}
