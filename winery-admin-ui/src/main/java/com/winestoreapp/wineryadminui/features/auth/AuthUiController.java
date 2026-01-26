package com.winestoreapp.wineryadminui.features.auth;

import com.winestoreapp.wineryadminui.features.user.dto.UserLoginRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthUiController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(UserLoginRequestDto dto, HttpSession session, Model model) {
        try {
            authService.login(dto, session);
            return "redirect:/ui/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials");
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session);
        return "redirect:/login";
    }
}
