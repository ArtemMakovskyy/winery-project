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
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Controller
@RequestMapping("/ui/users")
@RequiredArgsConstructor
@Slf4j
public class UserUiController {

    private final UserService userService;
    private final Tracer tracer;

    @GetMapping("/role")
    @Observed(name = "ui.user.role_form")
    public String roleForm(Model model) {
        model.addAttribute("form", new UpdateRoleForm());
        return "user/user-role";
    }

    @PostMapping("/role")
    @Observed(name = "ui.user.update_role_submit")
    public String updateRole(@Valid @ModelAttribute("form") UpdateRoleForm form,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            tagSpan("validation.status", "failed");
            return "user/user-role";
        }

        tagSpan("target.user.id", form.getUserId());
        tagSpan("target.role", form.getRole());

        try {
            UserResponseDto updated = userService.updateUserRole(
                    form.getUserId(), form.getRole()
            );
            model.addAttribute("success", true);
            model.addAttribute("user", updated);
            tagSpan("status", "success");
        } catch (RuntimeException e) {
            tagSpan("error.type", "business_logic");
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user role", e);
            recordError(e);
            model.addAttribute("error", "An unexpected error occurred");
        }
        return "user/user-role";
    }

    private void tagSpan(String key, Object value) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, String.valueOf(value));
        }
    }

    private void recordError(Throwable e) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.error(e);
        }
    }
}
