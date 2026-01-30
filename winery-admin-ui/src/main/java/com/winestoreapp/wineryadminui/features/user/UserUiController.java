package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.features.user.dto.UpdateRoleForm;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui/users")
@RequiredArgsConstructor
@Slf4j
public class UserUiController {

    private final UserService userService;

    @GetMapping("/role")
    public String roleForm(Model model) {
        model.addAttribute("form", new UpdateRoleForm());
        return "user/user-role";
    }

    @PostMapping("/role")
    public String updateRole(@Valid @ModelAttribute("form") UpdateRoleForm form,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "user/user-role";
        }

        try {
            UserResponseDto updated = userService.updateUserRole(
                    form.getUserId(), form.getRole()
            );
            model.addAttribute("success", true);
            model.addAttribute("user", updated);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user role", e);
            model.addAttribute("error", "An unexpected error occurred");
        }
        return "user/user-role";
    }
}
