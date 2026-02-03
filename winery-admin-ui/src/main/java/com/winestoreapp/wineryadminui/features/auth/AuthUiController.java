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

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthUiController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginDto", new UserLoginRequestDto("", ""));
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute("loginDto") UserLoginRequestDto dto,
                          BindingResult bindingResult,
                          HttpSession session,
                          Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Login validation failed for user: {}. Errors: {}", dto.email(), bindingResult.getAllErrors());
            return "auth/login";
        }

        try {
            authService.login(dto, session);
            return "redirect:/ui/dashboard";
        } catch (Exception e) {
            log.error("Authentication failed for user: {}. Reason: {}", dto.email(), e.getMessage());
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session);
        return "redirect:/login";
    }
}
